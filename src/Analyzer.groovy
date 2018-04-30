/**
 * Created by Janine on 2017-12-18.
 */
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Analyzer {

    static main(def args) {
        //def project_root = "C:\\Users\\Janine\\Desktop\\CodeReviewTool"
        //REPLACE with your project root
        Path currentRelativePath = Paths.get("");
        def project_root = currentRelativePath.toAbsolutePath().toString();
        //System.out.println("Current relative path is: " + s);
        def outputfilename = project_root + "/" + "codereviewout.txt"
        Logger log = new Logger(outputfilename)
        String sourceCodeDir = project_root + "/out/" + "dump_html" //REPLACE with your dataset
        new File(sourceCodeDir).eachFile { file ->
            try {
                File f = new File(outputfilename);
                if(f.exists() && !f.isDirectory()) {
                    f.delete()
                }

                println "processing ${file.getName()}"

                log.append "--app-start--"
                log.append "processing ${file.getName()}"

                String contents = readFile(file.toString(), Charset.defaultCharset());

                Parser parser = new Parser(log)
                parser.process(contents)

                log.append "--app-end--"

                println "done"

            } catch (IOException ie) {
                ie.printStackTrace();

            }
        }
    }
    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


}