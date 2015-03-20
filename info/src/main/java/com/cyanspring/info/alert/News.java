package com.cyanspring.info.alert;

public class News implements Comparable<News>{
	private String Title ;
	private String PicturePath;
	private String Article;
	private String ChildSitePath;
	private String PostTime;
	
	public void New()
	{
		setTitle("");
		setPicturePath("");
		setArticle("");
		setChildSitePath("");
		setPostTime("");
	}
	
	@Override
	public int compareTo(News other) {
		int compare = other.getPostTime().compareTo(this.getPostTime());
		if (compare == 0)
		{
			compare = this.getTitle().compareTo(other.getTitle());
		}
		return compare;
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

	public String getPostTime() {
		return PostTime;
	}

	public void setPostTime(String postTime) {
		PostTime = postTime;
	}
}
