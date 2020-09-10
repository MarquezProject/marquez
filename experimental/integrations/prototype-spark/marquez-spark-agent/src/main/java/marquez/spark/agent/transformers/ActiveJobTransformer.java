package marquez.spark.agent.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import marquez.spark.agent.SparkListener;

public class ActiveJobTransformer implements ClassFileTransformer {
  private static final String TBI = "org.apache.spark.scheduler.ActiveJob";
  protected final ClassPool pool = new ClassPool(true);//ClassPool.getDefault();

  public static final String CODE = String.format("{ %s.registerActiveJob(this); }", SparkListener.class.getName());


  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (!className.replace('/', '.').equals(TBI)) {
      if (className.contains("ActiveJob"))
          System.out.println("  SKIP(" +className.replace('/', '.')+")");
      return classfileBuffer;
    }
    System.out.println("ActiveJobTransformer.transform(" +className+")");
    try {
      this.pool.insertClassPath(new ByteArrayClassPath(TBI, classfileBuffer));
      CtClass ctClass = this.pool.get(TBI);
      if (ctClass.isFrozen()) {
        System.err.println("  FROZEN!!!");
        return classfileBuffer;
      }
      System.out.println("Code: " + CODE );
      // TODO: figure out why this is needed
      this.pool.insertClassPath(new ClassClassPath(org.apache.spark.scheduler.Stage.class));
      CtConstructor[] constructors = ctClass.getConstructors();
      for (CtConstructor constructor : constructors) {
        if (constructor.callsSuper()) {
          constructor.insertAfter(CODE);
        }
      }
      return ctClass.toBytecode();
    } catch (Throwable throwable) {
      System.err.println("BLETCH!!!");
      throwable.printStackTrace();
      return classfileBuffer;
    }
  }
}
