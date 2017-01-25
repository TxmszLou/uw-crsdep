import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static final String[] COLOR = {"black", "red", "blue", "green", "lightblue", "purple", "gray"};
    public static final Pattern preqPattern = Pattern.compile("Prerequisite(.*?)\\.($|\\s)");
    public static final Pattern codePattern = Pattern.compile("(\\b[A-Z]{1,}\\b\\s){1,}[0-9]*");

    public static String t(String s) {
        return s.trim().replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ", "_");
    }

    public static String toDotFormat(LinkedHashMap<String, List<List<String>>> m) {
        String ret = "digraph G {\n";
        for (String k : m.keySet()) {
            int i = 0;
            for (List<String> v : m.get(k)) {
                ret += "    " + "edge [color=" + COLOR[i % 7] + "];\n";
                i++;
                for (String w : v) {
                    if (!w.trim().isEmpty()) {
                        ret += "    " + t(w) + " -> " + t(k) + ";\n";
                    }
                }
            }
            if (m.get(k).isEmpty()) {
                ret += "    " + t(k) + ";\n";
            }
        }
        ret += "}";
        return ret;
    }

    public static void handle(File in, String ext) throws IOException {
        File file = new File("./" + ext + "/" + in.getName().split("\\.")[0] + "." + ext);
        PrintStream out = new PrintStream(file);

        LinkedHashMap obj = new LinkedHashMap();
        Document doc = Jsoup.parse(in, "UTF-8");

        Elements links = doc.getElementsByTag("a");

        String code = links.get(links.size() - 4).text().split("\\s")[0];

        for (Element link : links) {
            String name = link.attr("name");
            Matcher m = preqPattern.matcher(link.text());
            if (!name.isEmpty()) {
                name = name.substring(0, name.length() - 3).toUpperCase () + " " + name.substring(name.length() - 3, name .length());
                if (m.find()) {
                    String preq = m.group(0).substring(13, m.group(0).length() - 1);

                    List<List<String>> acc = new ArrayList<List<String>>();
                    for (String term : preq.split(";")) {
                        List<String> part = new ArrayList<String>();
                        Matcher c = codePattern.matcher(term);
                        while (c.find()) {
                            part.add(c.group(0));
                        }
                        if (part.isEmpty()) {
                            part.add(term);
                        }
                        acc.add(part);
                    }

                    obj.put(name, acc);
                } else {
                    obj.put(name, new ArrayList<List<String>>());
                }
            }
        }
        if (ext.equals("dot")){
            out.println(toDotFormat(obj));
        } else {
            String nodes = JSONObject.toJSONString(obj);
            out.println(nodes);
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File("./crscat");
        File[] files = dir.listFiles();
        for (File in : files) {
            System.out.println("processing " + in.getName() + "...");
            handle(in, "dot");
            handle(in, "json");
        }
    }
}
