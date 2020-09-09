package marquez.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/** A backend that writes events to a file in json. */
@Slf4j
class FileBackend implements Backend {

  private final File file;
  private Writer writer = null;

  public FileBackend(File file) {
    this(file, initWriter(file));
  }

  FileBackend(File file, Writer writer) {
    this.file = file;
    this.writer = writer;
  }

  @VisibleForTesting
  static Writer initWriter(File file) {
    File parentFile = file.getParentFile();
    if (file.exists() && !file.isFile()) {
      log.error("Can't write Marquez calls. " + file + " exists and is not a file.");
    } else if (parentFile.exists() && !parentFile.isDirectory()) {
      log.error("Can't write Marquez calls. " + parentFile + " exists and is not a directory.");
    } else if (!parentFile.exists() && !parentFile.mkdirs()) {
      log.error("Can't write Marquez calls. " + parentFile + " can not be created.");
    } else {
      try {
        return new FileWriter(file, UTF_8, true);
      } catch (IOException e) {
        log.error("Can't write Marquez calls. " + file + " can not be written to.", e);
      }
    }
    return null;
  }

  @Override
  public void put(String path, String json) {
    write("put", path, json);
  }

  @Override
  public void post(String path, String json) {
    write("post", path, json);
  }

  private void write(String method, String path, String json) {
    if (writer != null) {
      Map<String, String> call = new LinkedHashMap<String, String>(3);
      call.put("method", method);
      call.put("path", path);
      call.put("payload", json);
      String line = Utils.toJson(call) + "\n";
      try {
        writer.append(line).flush();
      } catch (IOException e) {
        log.error(
            "Can't write Marquez call "
                + line
                + " . "
                + file
                + " can not be written to. We won't try again.",
            e);
        writer = null;
      }
    }
  }
}
