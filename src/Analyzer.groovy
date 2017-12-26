/**
 * Created by Janine on 2017-12-18.
 */
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths;

class Analyzer {

    static main(def args) {
        def project_root = "C:\\Users\\Janine\\Desktop\\CodeReviewTool"
        //REPLACE with your project root

        def outputfilename = project_root + "/" + "overprivout.txt"
        def capsAsPerSamsungFile = project_root + "/" + "Capabilities.csv"
        def allCapsFile = project_root + "/" + "capfull.csv"

        String sourceCodeDir = project_root + "/out/" + "dump_ast" //REPLACE with your dataset
        new File(sourceCodeDir).eachFile { file ->
            try {
                String contents = readFile(file.toString(), Charset.defaultCharset());

                Parser parser = new Parser()
                parser.process(contents)

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