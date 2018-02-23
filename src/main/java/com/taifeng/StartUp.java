package com.taifeng;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartUp {

	public static final ArrayBlockingQueue<Map<String, String>> URL_DETAIL = new ArrayBlockingQueue<Map<String, String>>(
			1000);
	
	public static boolean isOver = false;
	public static HttpUtil util = null;

	private static int threadNum = 4;
	
	public static String PATH = "/data/erp-crawler-test/";
	
	static {
		try {
			util = new HttpUtil();
			util.init();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		Executors.newSingleThreadExecutor().submit(new Read());

		ExecutorService es = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			es.execute(new Process());
		}
	}

}
