package com.appspot.soupscraper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

@SuppressWarnings("serial")
public class SoupScraperServlet extends HttpServlet {
   

   private static final String SCRAPE_JS = "scrape.js";
   private static final String JSON2_JS = "json2.js"; // http://www.json.org/js.html
   private static final String SIZZLE_JS = "sizzle.js"; // http://www.sizzlejs.com
   private static final String ENV_JS = "env.rhino.js"; // http://github.com/thatcher/env-js
   
   private static PrintWriter currentResponseWriter;

   public static void rhinolog(String str) {
      if (currentResponseWriter != null) {
         currentResponseWriter.println(str);
      }
      System.out.println(str);
   }
   
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      resp.setContentType("text/plain");
      currentResponseWriter = resp.getWriter();

      
      String xhtml = "";
      String urlStr = req.getParameter("url");
      //if (urlStr == null || urlStr.isEmpty())
      //   urlStr = "http://de.wikipedia.org/wiki/Hamburg";
      if (urlStr == null || urlStr.isEmpty()) {
         StringBuilder htmlBuilder = new StringBuilder();
         htmlBuilder.append("<html><body>");
         htmlBuilder.append("<div><h1 class=\"cow\">Mooo Cows!</h1><p class=\"moo\">Hello Cows...</p></div>");
         htmlBuilder.append("<div><h1 class=\"dog\">Wooof Dogs!</h1><p class=\"woof\">Hello Dogs...</p></div>");
         htmlBuilder.append("</body></html>");
         xhtml = htmlBuilder.toString();
      }
      else {
         try {
            xhtml = getHtmlFromURL(urlStr);
            /*
            XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacesFeature, false);
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            //reader.setFeature(Parser.ignorableWhitespaceFeature, true);
            
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            //DOMResult result = new DOMResult();
            StringWriter validhtml = new StringWriter();
            StreamResult result = new StreamResult(validhtml);
            trans.transform( new SAXSource(reader, new InputSource(new StringReader(xhtml))), result);
           // xhtml = validhtml.toString();
           */
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

             

      }
      
      
      Context cx = Context.enter();
      try {
         // Initialize the standard objects (Object, Function, etc.)
         // This must be done before scripts can be executed. Returns
         // a scope object that we use in later calls.
         Scriptable scope = cx.initStandardObjects();
         
         cx.evaluateString(scope, "var print = function(str){ " +
         		//"Packages.com.appspot.soupscraper.SoupScraperServlet.rhinolog(str);" +
         		" };", "<cmd>", 1, null);
         
         loadJS(cx, scope, ENV_JS, SIZZLE_JS, JSON2_JS, SCRAPE_JS );

         String scrape = req.getParameter("scrape");
         if (scrape == null) scrape = "\"HTML\"";
         
         cx.evaluateString(scope, "scrapeParams={}", "<cmd>", 1, null);
         ScriptableObject vars = (ScriptableObject) scope.get("scrapeParams", scope);
         vars.defineProperty("scrape", scrape, 0);
         vars.defineProperty("xhtml", xhtml, 0);
         
         Object result = cx.evaluateString(scope, "scrape.JSONrequest(scrapeParams.scrape, scrapeParams.xhtml);", "<cmd>", 1, null);
         resp.getWriter().println(Context.toString(result));
         
      }
      catch (Exception e) {
         resp.getWriter().println("FATAL Exception:"+e);
      } finally {
          // Exit from the context.
          Context.exit();
      }
   }

   private void loadJS(Context cx, Scriptable scope, String... fileNames) throws IOException {
      for (String fileName : fileNames) {
         InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream(fileName));
         cx.evaluateReader(scope, reader, fileName, 1, null);
      }
   }

   private String getHtmlFromURL(String contentUrl) {
      //String message = URLEncoder.encode("my message", "UTF-8");

      try {
          URL url = new URL(contentUrl);
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          //connection.setDoOutput(true);
          connection.setRequestMethod("GET");
          /*
          OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
          writer.write("message=" + message);
          writer.close();
          */
          if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
             return StringUtil.stream2String(connection.getInputStream(), -1);
          } else {
              // Server returned HTTP error code.
          }
      } catch (MalformedURLException e) {
          // ...
      } catch (IOException e) {
          // ...
      }
      return "";
   }
}
