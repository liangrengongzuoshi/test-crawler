package com.taifeng;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Read implements Runnable {

	private int page = 98;
	
	@Override
	public void run() {
		String backUrl = "http://company.hrmarket.net/";
		for (int i = 1; i <= page; i++) {
			try {
				String url = "http://company.hrmarket.net/home_" + i + ".hr";
				System.out.println("开始抓取url：" + url);
				
				Map<String, String> additionalHeaders = new HashMap<String, String>();
				
				additionalHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				additionalHeaders.put("Upgrade-Insecure-Requests", "1");
				additionalHeaders.put("Cookie", "td_cookie=18446744071559050980; HRRegister=value=EBCC43379D2C250060AC72467E601763676E05B4A00AD342; _ga=GA1.2.2082591293.1498532910; _gid=GA1.2.118744033.1498638210");
				additionalHeaders.put("Referer", backUrl);
				
				String html = StartUp.util.getStringFromHttpResponse(StartUp.util.get(url, additionalHeaders, null));
				if(html == null || html.length() <= 0){
					System.out.println("开始抓取url-null：" + url);
					continue;
				}
				Document jsoup = Jsoup.parse(html);
				
				Elements eles = jsoup.select("dl.alphabet_first dd");
				
				if(eles != null){
					for(Element e:eles){
						Elements aurl = e.select("div.brandIndex_sort_item a");
						if(aurl != null){
							String uu = aurl.get(0).attr("href");
							if(uu == null || uu.length() <=0){
								continue;
							}
							String deepUrl = "http://company.hrmarket.net/" + uu;
							System.out.println("添加详情：" + deepUrl);
							
							Map<String, String> map = new HashMap<String, String>();
							map.put("reffer", url);
							map.put("detail", deepUrl);
							
							StartUp.URL_DETAIL.put(map);
						}
					}
				}
				
				backUrl = url;
				Thread.sleep(3000);
			} catch (Exception e) {
				System.out.println("抓取异常：" + i + ";" + e.getMessage());
				e.printStackTrace();
			}
		}
		StartUp.isOver = true;
	}
}
