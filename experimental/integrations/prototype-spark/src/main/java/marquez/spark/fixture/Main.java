package marquez.spark.fixture;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;

public class Main {
  public static void main(String[] args) throws IOException {
    System.out.println("Main.main");



    SparkConf conf = new SparkConf()
        .setAppName("my-app")
        .setMaster("local[2]");
    try (JavaSparkContext sc = new JavaSparkContext(conf);) {
      List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
      JavaRDD<Integer> distData = sc.parallelize(data);
      JavaRDD<Integer> filter = distData.filter(v -> v %2 == 0);
      filter.persist(StorageLevel.MEMORY_ONLY());

      PrintWriter w = new PrintWriter(new FileWriter("/tmp/test.csv"));
      w.println("1,2,3");
      w.println("4,5,6");
      w.close();
      File out = new File("/tmp/out.csv");
      if (out.exists() && out.isDirectory()) {
        for (File f : out.listFiles()) {
          f.delete();
        }
        out.delete();
      }
      sc.textFile("file:///tmp/test.csv").distinct().saveAsTextFile("file:///tmp/out.csv");
    }
  }


}
