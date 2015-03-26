/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.strategy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.downstream.DownStreamManager;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.downstream.IOrderRouter;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.strategy.GlobalStrategySettings;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyFactory;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.util.ClassEnumerator;

public class StrategyFactory implements IStrategyFactory, ApplicationContextAware, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(StrategyFactory.class);
	
	@Autowired
	GlobalStrategySettings globalStrategySettings;
	
	@Autowired
	private ScheduleManager scheduleManager;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	DownStreamManager downStreamManager;

	@Autowired(required=false)
	IOrderRouter orderRouter; 
	
	private ApplicationContext applicationContext;
	
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	protected Map<String, Long> jarFiles = new HashMap<String, Long>();
	private String strategyDirectory = "strategies";
	private boolean initialised = false;
	
	class StrategyRecord {
		String beanName;
		IStrategy template;
		ApplicationContext ac;
		
		
		public StrategyRecord(String beanName, IStrategy template,
				ApplicationContext ac) {
			super();
			this.beanName = beanName;
			this.template = template;
			this.ac = ac;
		}

	}
	private Map<String, StrategyRecord> registry = new HashMap<String, StrategyRecord>();

	public Set<String> getStrategyNames() {
		return registry.keySet();
	}
	
	public boolean validStrategy(String name) {
		return registry.containsKey(name);
	}
	
	public IStrategy createStrategy(String name, Object... objects) throws Exception {
		if(!initialised)
			this.init();
		
		StrategyRecord strategyRecord = registry.get(name);
		if (strategyRecord == null){
			throw new StrategyException("Strategy hasn't been registered: "
					+ name,ErrorMessage.STRATEGY_NOT_REGISTERD);
		}

		IStrategy strategy;
		try {
			strategy = (IStrategy)strategyRecord.ac.getBean(strategyRecord.beanName);
		} catch (BeansException e){
			throw new StrategyException(e.getMessage(),ErrorMessage.STRATEGY_ERROR);
		}
		strategy.create(objects);
		
		IDownStreamSender sender = null;
		if(null != orderRouter) {
			sender = orderRouter.setRoute(downStreamManager, strategy.getDataObject());
		} else {
			sender = downStreamManager.getSender();
		}
		strategy.setSender(sender);
		
		strategy.setCheckAdjQuote(globalStrategySettings.isCheckAdjQuote());
		strategy.setValidateQuote(globalStrategySettings.isValidateQuote());
		return strategy;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	synchronized private void registerStrategies(ApplicationContext ac) throws StrategyException {
		String[] listBeans = ac.getBeanNamesForType(IStrategy.class, true, false);
		for(String beanName: listBeans) {
			IStrategy strategy = (IStrategy)ac.getBean(beanName);
			String strategyName = strategy.getStrategyName();
			if(null == strategyName)
				throw new StrategyException("Bean " + beanName + " doesn't provide a strategy name");
			StrategyRecord existing = registry.put(strategyName, new StrategyRecord(beanName, strategy, ac));
			if(null != existing)
				log.warn("Strategy " + strategyName + " being overriden");
			
			log.info("Registered strategy " + strategyName + " to bean " + beanName);
			
			//if its already loaded, its an update, need to inform clients
			if(initialised) {
				RemoteAsyncEvent updatEvent = strategy.createConfigUpdateEvent();
				try {
					eventManager.sendRemoteEvent(updatEvent);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

		}
	}
	
	synchronized public List<IStrategy> getAllStrategyTemplates() {
		List<IStrategy> templates = new ArrayList<IStrategy>();
		for(StrategyRecord record: registry.values()) {
			templates.add(record.template);
		}
		return templates;
	}
	
	synchronized public Map<String, FieldDef> getStrategyFieldDef(String strategyName) throws StrategyException {
		StrategyRecord record = registry.get(strategyName);
		if(null == record)
			throw new StrategyException("Strategy field def not found: " + strategyName);
		Map<String, FieldDef> fieldDefs = record.template.getCombinedFieldDefs();
		if(null == fieldDefs)
			throw new StrategyException("Field definition is null for this strategy: " + strategyName);
		return fieldDefs;
	}
	
	synchronized public List<String> getStrategyAmendableFields(String strategy) throws StrategyException {
		StrategyRecord record = registry.get(strategy);
		List<String> result = new ArrayList<String>();
		for(FieldDef fieldDef: record.template.getCombinedFieldDefs().values()) {
			if(fieldDef.isAmendable())
				result.add(fieldDef.getName());
		}
		return result;
	}
	
	synchronized private void loadStrategyFromJar(File file) throws StrategyException, IOException {
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		URL url = new URL("file:" + strategyDirectory + "/" + file.getName());
		//URLClassLoader childCL = new URLClassLoader(new URL[]{url}, threadCL);
		URLClassLoader childCL = new URLClassLoader(new URL[]{url});
		
		List<Class<?>> classes = ClassEnumerator.processJarfile(url, "com.cyanspring", childCL);

		JarFile jarFile;
		String jarPath = strategyDirectory + "/" + file.getName();
		try {
			jarFile = new JarFile(jarPath);    
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
		}
		for(Class<?> clazz : classes) {
			if(IStrategy.class.isAssignableFrom(clazz)) {
				log.info("discovered custom strategy: " + clazz);
				String xmlFile = "conf/" + clazz.getSimpleName()+ ".xml";

				InputStream inputStream = null;
				
				Thread.currentThread().setContextClassLoader(childCL);
				try {
					ZipEntry zipEntry = jarFile.getEntry(xmlFile);
					if(null == zipEntry) {
						log.warn("Strategy xml file is missing " + xmlFile);
						continue;
					}

					inputStream = jarFile.getInputStream(zipEntry);
					//inputStream = childCL.getResourceAsStream(xmlFile);
					
					if(null == inputStream) {
						log.error("Strategy xml file can not be opened " + xmlFile);
						continue;
					}
	
					log.info("Loading " + xmlFile + " from " + file.getName());
					GenericApplicationContext customAC = new GenericApplicationContext();
					customAC.setParent(applicationContext);
					XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(customAC);
					
					xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
					xmlReader.loadBeanDefinitions(new InputStreamResource(inputStream));
					customAC.refresh();
					registerStrategies(customAC);
				} finally {
					if(null != inputStream)
						inputStream.close();
					
					Thread.currentThread().setContextClassLoader(threadCL);
				}

			}
		}
		jarFile.close();
	}
	
	private void loadJars() {
		File dir = new File(strategyDirectory);
		if(!dir.exists())
			return;
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				String fileName = file.getName().toLowerCase();
				if(file.isFile() && fileName.endsWith(".jar"))
					return true;
				return false;
			}
			
		};
		File[] files = dir.listFiles(filter);
		for(File file: files) {
			Long existing = jarFiles.put(file.getName(), file.lastModified());
			if(null == existing || !existing.equals(file.lastModified())) {
				log.info("loading strategy jar file: " + file);
				try {
					loadStrategyFromJar(file);
				} catch (MalformedURLException e) {
					log.error(e.getMessage(), e);
				} catch (StrategyException e) {
					log.error(e.getMessage(), e);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public void init() throws StrategyException {
		registerStrategies(applicationContext);
		loadJars();
		scheduleManager.scheduleRepeatTimerEvent(5000, this, timerEvent);
		initialised = true;
	}
	
	public void uninit() {
		scheduleManager.cancelTimerEvent(timerEvent);
		registry.clear();
		initialised = false;
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event.equals(timerEvent)) {
			try {
				loadJars();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public String getStrategyDirectory() {
		return strategyDirectory;
	}

	public void setStrategyDirectory(String strategyDirectory) {
		this.strategyDirectory = strategyDirectory;
	}
	
	
}
