package org.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class JSONUtils {
	public static String fetchJsonString(URL url) {
		NetworkThread net = new NetworkThread(url);

		net.start();

		try {
			net.join();
		} catch (InterruptedException e) {
			// NetworkThread sets jsonString to "" if interrupted
		}

		return net.getJSONString();
	}

	public static JSONObject fetchJsonObject(URL url) throws IOException,
			JSONException {
		return new JSONObject(fetchJsonString(url));
	}

	public static JSONObject fetchJsonObject(String s) throws IOException,
			JSONException {
		return new JSONObject(s);
	}

	public static JSONArray fetchJsonArray(URL url) throws IOException,
			JSONException {
		return new JSONArray(fetchJsonString(url));
	}

	public static JSONArray fetchJsonArray(String s) throws IOException,
			JSONException {
		return new JSONArray(s);
	}

	private static class NetworkThread extends Thread {
		private static final int MIN_TIMEOUT = 5000; // milliseconds
		private long timeout = MIN_TIMEOUT;
		private long elapsed = 0;
		private static final int MAX_ATTEMPTS = 5;
		private URL mUrl;
		private StringBuffer jsonBuffer;

		public NetworkThread(URL url) {
			this.mUrl = url;
		}

		public void run() {
			try {
				for (int i = 0; i < MAX_ATTEMPTS; i++) {
					if (Thread.currentThread().isInterrupted()) {
						System.err.println("NetworkThread.run(): Interrupted");
						throw new InterruptedException(
								"NetworkThread.run(): Interrupted");
					}
					jsonBuffer = new StringBuffer();
					if (attempt(mUrl)) {
						return;
					} else {
						// TODO: There are probably better algorithms.
						// TODO: Maintain the timeout value across invocations.
						timeout = Math.max(elapsed,
								MIN_TIMEOUT + (long) (Math.pow(2, i) * 1000));
						// 5000, 6000, 9000, 13000, 21000
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				jsonBuffer = null;
				return;
			}
			// TODO: Throw an Exception?
			// System.out.println("BeerMe.NetworkThread: too many attempts");
		}

		public boolean attempt(URL url) throws InterruptedException {
			boolean success = true;

			BufferedReader in = null;
			long start = System.nanoTime();
			try {
				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout((int) timeout);
				urlConnection.setReadTimeout((int) timeout);

				in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream()));

				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					if (Thread.currentThread().isInterrupted()) {
						jsonBuffer = null;
						System.err
								.println("NetworkThread.attempt(): Interrupted");
						throw new InterruptedException(
								"NetworkThread.attempt(): Interrupted");
					}

					jsonBuffer.append(inputLine);
				}

			} catch (SocketTimeoutException e) {
				System.err.println("JSONUtils.NetworkThread: timeout");
				success = false;
			} catch (IOException e1) {
				System.err.println("JSONUtils.NetworkThread: IOException 1: "
						+ e1.getLocalizedMessage());
				success = false;
			} finally {
				elapsed = (System.nanoTime() - start) / 1000000;
				// System.out.println("Elapsed (ms): " + elapsed);
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e2) {
					System.out
							.println("JSONUtils.NetworkThread: IOException 2: "
									+ e2.getLocalizedMessage());
					success = false;
				}
			}

			return success;
		}

		public String getJSONString() {
			return (jsonBuffer == null ? "" : jsonBuffer.toString());
		}
	}
}