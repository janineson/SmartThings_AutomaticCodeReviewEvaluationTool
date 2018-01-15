/**
 * Created by Janine on 2017-12-18.
 */
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.regex.Matcher
import java.util.regex.Pattern

class Parser{

    int reliabilityViolationCount, securityViolationCount, maintainabilityViolationCount, noViolationsCount
    def cyclomaticComplexity, abcMetric, methodCount, methodSize, linesOfCode, totalApps
    Logger log
    ArrayList<String> ruleViolationList
    Map<String, Integer> combinedViolationList

    public Parser(Logger logger) {

         reliabilityViolationCount = 0
         securityViolationCount = 0
         maintainabilityViolationCount= 0
         cyclomaticComplexity= 0
         abcMetric= 0
         noViolationsCount = 0
         log = logger
         ruleViolationList = new ArrayList()
         combinedViolationList=  new HashMap<>()

    }

    ArrayList<String> reliabilityList = new ArrayList<String>(
            Arrays.asList("AvoidRecurringShortSchedules", "NoBusyLoop", "NoSynchronized",
                    "AvoidChainedRunInCall", "UnusedArray", "UnusedObject",
                    "UseConsistentReturnValue", "VerifyArrayIndex", "HandleNullValue","MissingSwitchDefault",
                    "AssignmentInConditional", "BitwiseOperatorInConditional", "BrokenNullCheck," ,
                    "BrokenOddnessCheck", "ComparisonOfTwoConstants",  "ComparisonWithSelf",
                    "ConstantIfExpression", "ConstantTernaryExpression", "DuplicateMapKey",
                    "DuplicateSetValue", "EmptyCatchBlock", "EmptyElseBlock", "EmptyFinallyBlock",
                    "EmptyForStatement", "EmptyIfStatement", "EmptyMethod", "EmptySwitchStatement",
                    "EmptyTryBlock", "EmptyWhileStatement", "RandomDoubleCoercedToZero", "ReturnFromFinallyBlock",
                    "ThrowExceptionFromFinallyBlock", "ParameterReassignment", "MethodSize", "MethodCount",
                    "NoMissingEventHandler", "SingleArgEventHandler", "NoGlobalVariable", "AtomicStateUsage",
                    "AtomicStateUpdateUsage", "DeadCode")

    );
    ArrayList<String> maintainabilityList = new ArrayList<String>(
            Arrays.asList("SeparateParentChildApp", "CyclomaticComplexity", "NestedBlockDepth","AbcMetric",
                    "ConfusingTernary", "CouldBeElvis", "IfStatementCouldBeTernary", "InvertedIfElse",
                    "TernaryCouldBeElvis", "ForLoopShouldBeWhileLoop", "DoubleNegative")
    );
    ArrayList<String> securityList = new ArrayList<String>(
            Arrays.asList("DocumentExternalHTTPRequests", "DocumentExposedEndpoints", "ClearSubscription",
                    "SpecificSubscription", "NoDynamicMethodExecution", "NoHardcodeSMS", "NoRestrictedMethodCalls")
    );

    void process(String obj) {
        def ruleName, rulePriority, ruleLineNo, ruleSource
        String[] fileRuleSource

        Document doc = Jsoup.parse(obj)

        Elements divs = doc.select('div.summary');

        boolean flag = true
        for (Element div : divs) {

            Iterator<Element> headerIterator = div.select("h3").iterator() //header
            Iterator<Element> tableIterator = div.select("td").iterator()

            if (flag){
                def dummy = tableIterator.next().text()
                totalApps = tableIterator.next().text()
                flag = false
            }


            while(headerIterator.hasNext()){
                log.append("FILENAME : "+headerIterator.next().text())


                while (tableIterator.hasNext()) {

                    //violations and priority num
                    ruleName = tableIterator.next().text()
                    rulePriority = tableIterator.next().text()
                    ruleLineNo = tableIterator.next().text()
                    ruleSource = tableIterator.next().text()
                    fileRuleSource = ruleSource.split(" ")

                    /* Checks the cyclomatic complexity for methods/classes. A method
                    (or "closure field") with a cyclomatic complexity value greater
                    than the maxMethodComplexity property (20) causes a violation.
                    Likewise, a class that has an (average method) cyclomatic complexity
                    value greater than the maxClassAverageMethodComplexity property (20) causes a violation.
                     */

                    if (ruleName == "CyclomaticComplexity") {
                        cyclomaticComplexity = fileRuleSource[fileRuleSource.length - 1]//position of the value in the source line message
                        cyclomaticComplexity=getBracketValue(cyclomaticComplexity)
                    }

                    /*Checks the ABC size metric for methods/classes. A method (or "closure field")
                     with an ABC score greater than the maxMethodAbcScore property (60) causes a violation.
                     Likewise, a class that has an (average method) ABC score greater than the
                     maxClassAverageMethodAbcScore property (60) causes a violation.*/
                    if (ruleName == "AbcMetric") {
                        abcMetric = fileRuleSource[fileRuleSource.length - 1]
                        abcMetric=getBracketValue(abcMetric)

                    }
                    /*A class with too many methods is probably a good suspect for refactoring,
                    in order to reduce its complexity and find a way to have more fine grained
                    objects.The maxMethods property (30) specifies the threshold.
                     */
                    if (ruleName == "MethodCount")
                        methodCount = fileRuleSource[fileRuleSource.length - 2]
                    //Checks if the size of a method exceeds the number of lines specified by the maxLines property (100).
                    if (ruleName == "MethodSize")
                        methodSize = fileRuleSource[fileRuleSource.length - 2]

                    if (ruleName == "TotalLinesOfCode")
                        linesOfCode = fileRuleSource[fileRuleSource.length - 2]
                    else{
                        log.append("rule name : " + ruleName + " line : " + ruleLineNo)
                        log.append("source line/ message : " + ruleSource)

                        ruleViolationList.add(ruleName)
                    }

                }

                displayQualityAttribute()
                resetCount()

            }


        }
        log.append("Total SmartApps Analyzed : " + totalApps)
        log.append("Total SmartApps with Violations : " + (Integer.parseInt((String)totalApps)-noViolationsCount))
        log.append("---Most Common Violations---")
        for (String keys : sortByValues(combinedViolationList).keySet())
        {
            log.append(keys + ":"+ combinedViolationList.get(keys))
        }
    }

     Map<String, Integer> sortByValues(Map<String, Integer> map) {
        Comparator<String> valueComparator =  new Comparator<String>() {
            int compare(String k1, String k2) {
                int compare = map.get(k2).compareTo(map.get(k1))
                if (compare == 0) return 1
                else return compare;
            }
        }
        Map<String, Integer> sortedByValues = new TreeMap<String, Integer>(valueComparator)
        sortedByValues.putAll(map)
        return sortedByValues
    }

    def getBracketValue(def string){
        Pattern p = Pattern.compile("\\[(.*?)\\]")
        Matcher m = p.matcher(string)
        while (m.find()) {
            string = m.group(1)
        }
        return string
    }


    void resetCount(){
        reliabilityViolationCount = 0
        securityViolationCount = 0
        maintainabilityViolationCount = 0

        ruleViolationList = new ArrayList()

        cyclomaticComplexity = 0
        abcMetric = 0
    }
    void defectCount(){

        for (String rule : ruleViolationList) {
            if(reliabilityList.contains(rule)){
                reliabilityViolationCount++
            }

            if(securityList.contains(rule)){
                securityViolationCount++
            }

            if(maintainabilityList.contains(rule)){
                maintainabilityViolationCount++
            }
        }

    }

    /*
    Defect density is the number of defects found in the software product
    per size of the code. It can be defined as the number of defects per 1,000 lines of code or function points.
     */
    Float calculateRate(int count){
        return (count / Integer.parseInt(linesOfCode)) * 1000
    }

    void violations(){

        for (String rule : reliabilityList) {
            if (ruleViolationList.count(rule) > 0){
                if (rule == 'MethodCount')
                    log.append(rule + " : "  + methodCount)
                else if (rule == 'MethodSize')
                    log.append(rule + " : "  + methodSize)
                else
                    log.append(rule + " : "  + ruleViolationList.count(rule))

                setCombinedViolations(rule, ruleViolationList.count(rule))
            }
        }

        for (String rule : securityList) {
            if (ruleViolationList.count(rule) > 0){
                log.append(rule + " : "  + ruleViolationList.count(rule))
                setCombinedViolations(rule, ruleViolationList.count(rule))
            }

        }

        for (String rule : maintainabilityList) {
            if (ruleViolationList.count(rule) > 0){
                if (rule == 'CyclomaticComplexity')
                    log.append(rule + " : "  + cyclomaticComplexity)
                else if (rule == 'AbcMetric')
                    log.append(rule + " : "  + abcMetric)
                else
                    log.append(rule + " : "  + ruleViolationList.count(rule))
                setCombinedViolations(rule, ruleViolationList.count(rule))
            }
        }
        log.append("Total Violations : " + ruleViolationList.size())
        if (ruleViolationList.size() == 0) noViolationsCount++
    }

    void setCombinedViolations(String rule, int count){
        if(combinedViolationList.containsKey(rule))
            combinedViolationList.put(rule, combinedViolationList.get(rule) + count)
        else
            combinedViolationList.put(rule, count)
    }


    void displayQualityAttribute(){
        log.append("---Defect Density Metrics (KLOC)---")
        defectCount()

        log.append("Reliability - " + calculateRate(reliabilityViolationCount).round(2))
        log.append("Security - "  + calculateRate(securityViolationCount).round(2))
        log.append("Maintainability - "  + calculateRate(maintainabilityViolationCount).round(2))
        log.append("Total Defect Density - " + calculateRate(reliabilityViolationCount+
                maintainabilityViolationCount+securityViolationCount).round(2))


        log.append("---Breakdown of Violations and Other Metrics---")
        log.append("Lines of Code : " + linesOfCode)
        violations()
        log.append(" ")
    }

}