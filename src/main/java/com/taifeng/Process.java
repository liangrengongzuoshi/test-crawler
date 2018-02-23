package com.taifeng;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Process implements Runnable {

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			Map<String, String> map = StartUp.URL_DETAIL.poll();
			
			if(map == null){
				if(StartUp.isOver){
					System.out.println("抓取完毕.");
					System.exit(0);
				}else{
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
					}
					continue;
				}
			}

			try {
				String reffer = map.get("reffer");
				String detail = map.get("detail");
				
				System.out.println("开始抓取详情页：" + detail);
				Map<String, String> additionalHeaders = new HashMap<String, String>();
				
				additionalHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				additionalHeaders.put("Upgrade-Insecure-Requests", "1");
				additionalHeaders.put("Cookie", "td_cookie=18446744071559580245; _gat=1; HRRegister=value=EBCC43379D2C2500990E811A1C404787041A0B30787B9E51; _ga=GA1.2.2082591293.1498532910; _gid=GA1.2.118744033.1498638210");
				additionalHeaders.put("Referer", reffer);
				
				String html = StartUp.util.getStringFromHttpResponse(StartUp.util.get(detail, additionalHeaders, null));
				
				if(html == null || html.length() <=0){
					System.out.println("处理detail异常-null" + detail);
					continue;
				}
				if(html.length() <= 1100){
					html = StartUp.util.getStringFromHttpResponse(StartUp.util.get(detail, additionalHeaders, null));
				}
						
				Document jsoup = Jsoup.parse(html);
				StringBuilder sb = new StringBuilder();
				
				// 公司名
				String comName = getText(jsoup.select("#ComDetailNavigation1_lblCompanyName"));
				sb.append(comName).append("\t");
				// System.out.println(comName);
				// 专注领域 .specialties p
				String zzly = getText(jsoup.select(".specialties p"));
				sb.append(zzly).append("\t");
				// System.out.println(zzly);
				// 公司网站 
				String comUrl = getText(jsoup.select("li.website p a"));
				sb.append(comUrl).append("\t");
				// System.out.println(comUrl);
				// 公司类型
				String comLx = getText(jsoup.select("div.basic-info-about li.type p"));
				sb.append(comLx).append("\t");
				// System.out.println(comLx);
				// 公司总部
				String comZb = getText(jsoup.select("span.country-name"));
				sb.append(comZb).append("\t");
				// System.out.println(comZb);
				
				// 公司人数
				String comRs = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(3) > li.company-size.word-wrap > p"));
				sb.append(comRs).append("\t");
				// System.out.println(comRs);
				// 创立年份
				String comYear = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(3) > li:nth-child(2) > p"));
				sb.append(comYear).append("\t");
				// System.out.println(comYear);
				// 注册资金
				String comMoney = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(3) > li:nth-child(3) > p"));
				sb.append(comMoney).append("\t");
				// System.out.println(comMoney);
				// 上市情况
				String comSs = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(4) > li.company-size.word-wrap > p"));
				sb.append(comSs).append("\t");
				// System.out.println(comSs);
				// 证券交易所
				String comZq = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(4) > li:nth-child(2) > p"));
				sb.append(comZq).append("\t");
				// System.out.println(comZq);
				// 股票代码
				String comGp = getText(jsoup.select("#content > div.basic-info.viewmore-container.abbreviated.with-image > div.basic-info-about > ul:nth-child(4) > li:nth-child(3) > p > a"));
				sb.append(comGp).append("\t");
				// System.out.println(comGp);
				sb.append(detail).append("\n");
				
				String path = StartUp.PATH + Thread.currentThread().getName() + ".txt";
				
				FileUtils.appendFile(path, sb.toString());
				
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("处理detail异常：" + map.get("reffer") + ";" + map.get("detail"));
				e.printStackTrace();
			}
		}
	}
	
	private String getText(Elements eles){
		if(eles == null || eles.size() <= 0){
			return null;
		}
		return eles.get(0).text();
	}

}
