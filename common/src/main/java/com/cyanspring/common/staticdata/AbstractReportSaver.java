package com.cyanspring.common.staticdata;

public abstract class AbstractReportSaver {

	protected FileManager fileManager;
	protected String filePath;
	protected String prefix = "";
	protected String suffix = "";

    public void saveToFile() {}

	public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
