package com.cyanspring.info.alert;

public class News implements Comparable<News>{
	private String Title ;
	private String PicturePath;
	private String Article;
	private String ChildSitePath;
	
	public void New()
	{
		setTitle("");
		setPicturePath("");
		setArticle("");
		setChildSitePath("");
	}
	@Override
	public int compareTo(News other) {
		int compare = this.getChildSitePath().compareTo(other.getChildSitePath());
		return compare;
	}
	
	@Override
	public boolean equals(Object obj){		
		return this.getChildSitePath().equals(((News)obj).getChildSitePath());	
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getPicturePath() {
		return PicturePath;
	}
	public void setPicturePath(String picturePath) {
		PicturePath = picturePath;
	}
	public String getArticle() {
		return Article;
	}
	public void setArticle(String article) {
		Article = article;
	}
	public String getChildSitePath() {
		return ChildSitePath;
	}
	public void setChildSitePath(String childSitePath) {
		ChildSitePath = childSitePath;
	}
}
