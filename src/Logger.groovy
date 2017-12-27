/**
 * Created by Janine on 2017-12-27.
 */
class Logger
{
    File file

    public Logger(def filename)
    {
        file = new File(filename)
    }

    public void append(String s)
    {
        file.append(System.getProperty("line.separator") + s)
    }
}
