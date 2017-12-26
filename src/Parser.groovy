/**
 * Created by Janine on 2017-12-18.
 */
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class Parser{

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
        def ruleName, rulePriority, ruleLineNo, ruleSource, linesOfCode
        String[] fileLOC

        Document doc = Jsoup.parse(obj)

        Elements divs = doc.select('div.summary');

        for (Element div : divs) {
            Iterator<Element> headerIterator = div.select("h3").iterator() //header

            while(headerIterator.hasNext()){
                System.out.println("FILENAME : "+headerIterator.next().text())

                Iterator<Element> tableIterator = div.select("td").iterator()

                while (tableIterator.hasNext()) {

                    //violations and priority num
                    ruleName = tableIterator.next().text()
                    rulePriority = tableIterator.next().text()
                    ruleLineNo = tableIterator.next().text()
                    ruleSource = tableIterator.next().text()
                    fileLOC = ruleSource.split(" ")


                    if (ruleName == "TotalLinesOfCode")
                        linesOfCode = fileLOC[fileLOC.length - 2];
                    else{
                        System.out.println("rule name : " + ruleName + " line : " + ruleLineNo)
                        System.out.println("source line/ message : " + ruleSource)

                        ruleViolationList.add(ruleName)
                    }

                }

                System.out.println("lines of code : " + linesOfCode)
                displayQualityAttribute(linesOfCode)
                resetCount()

            }

        }

    }

    int reliabilityViolationCount
    int securityViolationCount
    int maintainabilityViolationCount
    ArrayList<String> ruleViolationList = new ArrayList()

    void resetCount(){
        reliabilityViolationCount = 0
        securityViolationCount = 0
        maintainabilityViolationCount = 0

        ruleViolationList = new ArrayList()
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

    Float calculateRate(int count, def linesOfCode){

        return 100 - (count / Integer.parseInt(linesOfCode)) * 100
    }

    void metrics(){
        boolean noViolation = true

        for (String rule : reliabilityList) {
            if (ruleViolationList.count(rule) > 0){
                println(rule + " : "  + ruleViolationList.count(rule))
                noViolation = false
            }
        }

        for (String rule : securityList) {
            if (ruleViolationList.count(rule) > 0){
                println(rule + " : "  + ruleViolationList.count(rule))
                noViolation = false
            }

        }

        for (String rule : maintainabilityList) {
            if (ruleViolationList.count(rule) > 0){
                println(rule + " : "  + ruleViolationList.count(rule))
                noViolation = false
            }
        }

        if (noViolation)
            println("NONE")
    }

    void displayQualityAttribute(def linesOfCode){
        println("---Quality Rate---")
        defectCount()

        println("Reliability - " + calculateRate(reliabilityViolationCount, linesOfCode).round(2) + "%")
        println("Security - "  + calculateRate(securityViolationCount, linesOfCode).round(2)  + "%")
        println("Maintainability - "  + calculateRate(maintainabilityViolationCount, linesOfCode).round(2)  + "%")

        println("---Violation Metrics---")
        metrics()
        println()
    }

}