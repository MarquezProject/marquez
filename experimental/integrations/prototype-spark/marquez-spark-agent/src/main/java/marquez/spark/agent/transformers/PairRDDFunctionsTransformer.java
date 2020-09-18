package marquez.spark.agent.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import marquez.spark.agent.SparkListener;

public class PairRDDFunctionsTransformer implements ClassFileTransformer {
  private static final Logger logger = LoggerFactory.getLogger(PairRDDFunctionsTransformer.class);
  private static final String TBI = "org.apache.spark.rdd.PairRDDFunctions";
  protected final ClassPool pool = ClassPool.getDefault();

  public static final String CODE = String.format("{ %s.registerOutput(this, conf); }", SparkListener.class.getName());


  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (!className.replace('/', '.').equals(TBI)) {
      return classfileBuffer;
    }
    logger.info("PairRDDFunctionsTransformer.transform(" +className+")");
    try {
      this.pool.insertClassPath(new ByteArrayClassPath(TBI, classfileBuffer));
      CtClass ctClass = this.pool.get(TBI);
      if (ctClass.isFrozen()) {
        logger.error(className + " is frozen. Not doing anything");
        return classfileBuffer;
      }
      ctClass.getDeclaredMethod("saveAsNewAPIHadoopDataset").insertBefore(CODE);
      ctClass.getDeclaredMethod("saveAsHadoopDataset").insertBefore(CODE);
      return ctClass.toBytecode();
    } catch (Throwable throwable) {
      System.err.println("BLETCH!!!");
      throwable.printStackTrace();
      return classfileBuffer;
    }
  }
}
