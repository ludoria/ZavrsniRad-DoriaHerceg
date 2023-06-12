import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Solution {

    public static void main(String[] args) throws IOException {
        Path trainFile = null;
        Path testFile = null;
        int treeDepth = -1;

        if(args.length == 2) {
            trainFile = Path.of(args[0]);
            testFile = Path.of(args[1]);
        } else if(args.length == 3) {
            trainFile = Path.of(args[0]);
            testFile = Path.of(args[1]);
            treeDepth = Integer.parseInt(args[2]);
        } else System.err.println("Ulaz je kriv. Probaj ponovno.");

        DataLoader loader = new DataLoader();
        List<List<String>> data = loader.loadData(trainFile);
        List<List<String>> testData = loader.loadData(testFile);

        DecisionTree model = new DecisionTree(treeDepth);
        DecisionTree.Node tree = model.fit(data);
        System.out.print("[PREDICTIONS]: ");
        model.predict(data,testData,tree);
    }
}