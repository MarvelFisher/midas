package com.cyanspring.id.gateway.netty;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.gateway.IdGateway;
import com.cyanspring.id.gateway.UserClient;
import com.thoughtworks.xstream.io.path.Path;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class WebTask {

	final static String strXmlFmt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Response cmd=\"%s\">";
	final static String strXmlEnd = "</Response>";

	static String formatSize(double dSize) {
		final int KB = 1024;
		final int MB = 1024 * KB;
		final int GB = KB * MB;
		final String _defSizeValue = "       0";

		String strRet = _defSizeValue;
		if (dSize > GB) {
			strRet = String.format("%.5f GB", dSize / GB);
		} else if (dSize > MB) {
			strRet = String.format("%.4f MB", dSize / MB);
		} else if (dSize > KB) {
			strRet = String.format("%.3f KB", dSize / KB);
		} else if (dSize != 0)
			strRet = String.format("%d bytes", (int) dSize);

		return strRet;
	}

	public static String getLastModified() {
		java.nio.file.Path path = Paths.get("");
		String sFile = String.format("%s/jars/cyanspring-adaptor-2.56.jar", path.toAbsolutePath().toString());
		java.io.File file = new java.io.File(sFile);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (file == null) {
			return sdf.format(DateUtil.now());
		}
		long lastModified = file.lastModified();
		return sdf.format(lastModified);
	}
	
	public static void onTask(ChannelHandlerContext ctx, HttpUrlParam params) {
		String cmd = params.getCmd();
		switch (cmd.toLowerCase()) {
		case "client": {
			WebTask.getConnect(ctx, cmd);
		}
			break;
		case "recon": {
			WebTask.reconnect(ctx, cmd);
		}
			break;
		case "status": {
			WebTask.status(ctx, cmd);
		}
			break;
		default: {
			WebTask.error(ctx, params.getUrl());
			break;
		}
		}
	}

	static String getClientInfo() {
		StringBuilder sb = new StringBuilder();
		List<UserClient> list = ServerHandler.getClients();
		if (list.size() > 0) {
			sb.append("<Clients>");
			for (UserClient client : list) {
				sb.append(client.toXml());
			}
			sb.append("</Clients>");
		}
		list.clear();
		return sb.toString();
	}

	public static void getConnect(ChannelHandlerContext ctx, String cmd) {

		// get clients info
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(strXmlFmt, cmd));
		sb.append(getClientInfo());
		sb.append(strXmlEnd);

		// send
		HttpServerHandler.sendData(ctx, HttpResponseStatus.OK, sb.toString());

	}

	public static void reconnect(ChannelHandlerContext ctx, String cmd) {

		// exec command
		IdGateway.instance().closeClient();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(strXmlFmt, cmd));
		sb.append(strXmlEnd);
		// send
		HttpServerHandler.sendData(ctx, HttpResponseStatus.OK, sb.toString());
	}

	public static void status(ChannelHandlerContext ctx, String cmd) {

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(strXmlFmt, cmd));
		String sIp = IdGateway.instance().getReqIp();
		int nPort = IdGateway.instance().getReqPort();
		sb.append(String.format("<JarFile>%s</JarFile>", getLastModified()));
		sb.append(String.format("<Source>%s:%d</Source>", sIp, nPort));
		Date dt = ClientHandler.lastRecv;
		sb.append(String.format("<LastReceive>%s</LastReceive>",
				DateUtil.formatDate(dt, "yyyy-MM-dd HH:mm:ss")));
		long inNo = IdGateway.instance().inNo;
		double dInSize = IdGateway.instance().inSize;
		sb.append(String.format("<InSize Count=\"%d\">%s</InSize>", inNo,
				formatSize(dInSize)));
		long outNo = IdGateway.instance().outNo;
		double dOutsize = IdGateway.instance().outSize;
		sb.append(String.format("<OutSize Count=\"%d\">%s</OutSize>", outNo,
				formatSize(dOutsize)));
		sb.append(getClientInfo());
		sb.append(strXmlEnd);

		// send
		HttpServerHandler.sendData(ctx, HttpResponseStatus.OK, sb.toString());
		sb.setLength(0);
		sb = null;
	}

	public static void error(ChannelHandlerContext ctx, String strUrl) {
		final String strfmt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Response>\n<Error Message=\"unrecognized  command : Url[%s]\" />\n</Response>";

		HttpServerHandler.sendData(ctx, HttpResponseStatus.OK,
				String.format(strfmt, strUrl));

	}
}
