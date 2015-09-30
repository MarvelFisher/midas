package com.cyanspring.info.ne.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.Consts;

public class HttpHelper
{
	public static String post(String uri, HashMap<String, String> headers, HashMap<String, String> forms)
			throws ClientProtocolException, IOException
	{
		Request post = Request.Post(uri);
		if (headers != null && headers.size() != 0)
		{
			for (Entry<String, String> entry : headers.entrySet())
			{
				post = post.addHeader(entry.getKey(), entry.getValue());
			}
		}

		if (forms != null && forms.size() != 0)
		{
			Form form = Form.form();
			for (Entry<String, String> entry : forms.entrySet())
			{
				form = form.add(entry.getKey(), entry.getValue());
			}
			post.bodyForm(form.build(), Consts.UTF_8);
		}

		return post.execute().returnContent().asString();
	}
}
