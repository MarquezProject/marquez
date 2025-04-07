package test;

import io.prometheus.client.exporter.jakarta.MetricsServlet;

public class TestMetrics {
    public static void main(String[] args) {
        System.out.println("Hello from MetricsServlet test!");
        MetricsServlet servlet = new MetricsServlet();
        System.out.println("Created servlet: " + servlet.getClass().getName());
    }
}
