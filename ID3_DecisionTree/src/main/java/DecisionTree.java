import java.text.DecimalFormat;
import java.util.*;

public class DecisionTree {
    private final int depth;
    private List<String> znacajkeICilj;

    public static class Node {
        String vrijednost;
        Map<String, Node> subtrees;

        //node
        private Node(String vrijednost, Map<String, Node> subtrees) {
            this.vrijednost = vrijednost;
            this.subtrees = subtrees;
        }

        //leaf
        private Node(String vrijednost) {
            this.vrijednost = vrijednost;
            this.subtrees = null;
        }
    }

    public DecisionTree(int depth) {
        this.depth  = depth;
    }

    public Node fit(List<List<String>> data) {
        znacajkeICilj = data.remove(0);
        List<String> znacajke = new ArrayList<>();
        for(int i = 0; i < znacajkeICilj.size()-1; i++) {
            znacajke.add(znacajkeICilj.get(i));
        }
        Map<String, Double> y = getVrijednostiCiljneVarijable(data);
        Node node = id3(data, data, znacajke, y, 0);
        System.out.println("[BRANCHES]:");
        printNode(node, 1, new ArrayList<>());

        return node;
    }

    public void predict(List<List<String>> trainData, List<List<String>> testData, Node tree) {
        Node currentNode = tree;
        List<String> znacajke = testData.remove(0);
        List<String> y = vrijednostiZnacajke(testData, testData.get(0).size()-1);
        Map<String, Double> ciljneVarijable = getVrijednostiCiljneVarijable(trainData);
        Map<String, Double> podjelaKlasaUTestnimPodacima = getVrijednostiCiljneVarijable(testData);
        Map<String, Double> sortedVarijable = new TreeMap<>(ciljneVarijable);
        int[][] confusionMatrix = new int[y.size()][y.size()];
        for(List<String> primjer: testData) {
            while(!Objects.isNull(currentNode.subtrees)) {
                int index = znacajke.indexOf(currentNode.vrijednost);
                String vrijednost = primjer.get(index);
                currentNode = currentNode.subtrees.get(vrijednost);
                if(Objects.isNull(currentNode)) {
                    String v = "";
                    Double num = -1.0;
                    for(Map.Entry<String,Double> entry : sortedVarijable.entrySet()) {
                        if(entry.getValue() > num) {
                            num = entry.getValue();
                            v = entry.getKey();
                        }
                    }
                    currentNode = new Node(v);
                }
            }
            System.out.print(currentNode.vrijednost + " ");
            Collections.sort(y);
            int indexStvarneVrijednosti = y.indexOf(primjer.get(primjer.size()-1));
            int indexCilja = y.indexOf(currentNode.vrijednost);
            confusionMatrix[indexStvarneVrijednosti][indexCilja]++;
            currentNode = tree;
        }
        System.out.println();
        double tocnoPredvideni = 0.0;
        System.out.println("[CONFUSION_MATRIX]:");
        for(int i = 0; i < confusionMatrix.length; i++) {
            tocnoPredvideni += confusionMatrix[i][i];
            for (int j = 0; j < confusionMatrix.length-1; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.print(confusionMatrix[i][confusionMatrix.length-1]);
            System.out.println();
        }
        System.out.println("[ACCURACY]: " + round(tocnoPredvideni/testData.size()));
        if(confusionMatrix[0].length == 2) {

            System.out.println("[SENSITIVITY]: " + round(confusionMatrix[0][0]/podjelaKlasaUTestnimPodacima.get("2")));
            System.out.println("[SPECIFITY]: " + round((double) confusionMatrix[1][1] /(confusionMatrix[1][1] + confusionMatrix[1][0])));
        }
    }

    public static String round(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00000");
        return decimalFormat.format(value);
    }

    private List<String> printNode(Node node, int counter, List<String> branch) {
        Node currentNode = node;
        if(!Objects.isNull(currentNode.subtrees)) {
            for(Map.Entry<String, Node> subtree: currentNode.subtrees.entrySet()) {
                if(counter == branch.size()) branch.remove(branch.size()-1);
                branch.add(counter + ":" + node.vrijednost + "=" + subtree.getKey());
                currentNode = subtree.getValue();
                branch = printNode(currentNode, counter+1, branch);
                if(counter == 1) branch.removeAll(branch);
            }
        } else {
            branch.add(currentNode.vrijednost);
            String s = "";
            for(int i = 0; i < branch.size()-1; i++) {
                s += branch.get(i) + " ";
            }
            s += branch.get(branch.size()-1);
            System.out.println(s);
            branch.remove(branch.size()-1);
            branch.remove(branch.size()-1);
            return branch;
        }
        return branch;
    }

    public Node id3(List<List<String>> skupOznacenihPrimjera, List<List<String>> dParentSkup, List<String> znacajke, Map<String, Double> vrijednostiCiljneVarijable, int dubina) {
        //koliko ima kojih ciljnih oznaka (yes, no, maybe)
        Map<String, Double> raspodjelaCiljnihOznaka = getVrijednostiCiljneVarijable(dParentSkup);

        if(skupOznacenihPrimjera.isEmpty()) {
            //ovo je zapravo leaf
            return new Node(Collections.max(raspodjelaCiljnihOznaka.entrySet(), Map.Entry.comparingByValue()).getKey());
        }

        Map<String, Double> vrijednostiCiljneVarijableSkupaOznacenihPrimjera = getVrijednostiCiljneVarijable(skupOznacenihPrimjera);
        Map<String, Double> sortedVrijednostiCiljneVarijableSkupaOznacenihPrimjera = new TreeMap<>(vrijednostiCiljneVarijableSkupaOznacenihPrimjera);
        String najcescaOznaka = Collections.max(sortedVrijednostiCiljneVarijableSkupaOznacenihPrimjera.entrySet(), Map.Entry.comparingByValue()).getKey();
        List<List<String>> skupPrimjeraSNajcescomOznakom = new ArrayList<>();
        for(List<String> oznaceniPrimjer: skupOznacenihPrimjera) {
            if(oznaceniPrimjer.get(oznaceniPrimjer.size()-1).equals(najcescaOznaka)) {
                skupPrimjeraSNajcescomOznakom.add(oznaceniPrimjer);
            }
        }

        if(znacajke.isEmpty() || skupOznacenihPrimjera.equals(skupPrimjeraSNajcescomOznakom) || depth == dubina) {
            //ovo je zapravo leaf
            return new Node(najcescaOznaka);
        }

        Double entropijaPocetnogSkupa = entropijaPocetnogSkupa(vrijednostiCiljneVarijable);
        Double maxIGValue = -1.0;
        String znacajkaNajveceIGVrijednosti = null;
        for(int i = 0; i < znacajke.size(); i++) {
            Map<String, List<Double>> particije = getParticijePoZnacajki(skupOznacenihPrimjera, i, znacajke);
            Double IG = IG(entropijaPocetnogSkupa, particije);
            if(IG > maxIGValue) {
                maxIGValue = IG;
                znacajkaNajveceIGVrijednosti = znacajke.get(i);
            }
        }
        Map<String, Node> subtrees = new HashMap<>();
        int index = znacajke.indexOf(znacajkaNajveceIGVrijednosti);
        String znacajkaKojuTrazim = znacajke.get(index);
        index = znacajkeICilj.indexOf(znacajkaKojuTrazim);
        String finalZnacajkaNajveceIGVrijednosti = znacajkaNajveceIGVrijednosti;
        znacajke = znacajke.stream().filter((e) -> !e.equals(finalZnacajkaNajveceIGVrijednosti)).toList();
        Node node = null;

        for(String vrijednostZnacajke: vrijednostiZnacajke(skupOznacenihPrimjera, index)) {
            List<List<String>> filtriraniPrimjeri = new ArrayList<>();
            for(List<String> primjer: skupOznacenihPrimjera) {
                if(primjer.get(index).equals(vrijednostZnacajke)) {
                    filtriraniPrimjeri.add(primjer);
                }
            }
            node = id3(filtriraniPrimjeri, skupOznacenihPrimjera, znacajke, vrijednostiCiljneVarijable, dubina+1);
            subtrees.put(vrijednostZnacajke, node);
        }

        return new Node(znacajkaNajveceIGVrijednosti, subtrees);
    }
    private Map<String, Double> getVrijednostiCiljneVarijable(List<List<String>> data) {
        Map<String, Double> map = new HashMap<>();
        for(List<String> primjer: data) {
            String key = primjer.get(primjer.size()-1);
            if(map.containsKey(key)) {
                map.put(key, map.get(key)+1);
            } else {
                map.put(key, 1.0);
            }
        }
        return map;
    }

    private double entropijaPocetnogSkupa(Map<String, Double> map) {
        return entropy(map.values().stream().toList());
    }

    private double entropy(List<Double> values) {
        double result = 0;
        double sum = 0;
        for(Double value: values) {
            sum += value;
        }
        for(double value: values) {
            if(value == 0) continue;
            result += -1 * (value/sum)*(Math.log(value/sum)/Math.log(2));
        }
        return result;
    }

    private Double IG(Double startSetEntropy, Map<String,List<Double>> particijePoZnacajki) {
        Double result = startSetEntropy;
        Map<String, Double> entropije = new HashMap<>();
        double sum = 0.0;
        for(Map.Entry<String,List<Double>> entry: particijePoZnacajki.entrySet()) {
            entropije.put(entry.getKey(), entropy(entry.getValue()));
            sum += entry.getValue().stream().mapToDouble(Double::doubleValue).sum();
        }

        for(Map.Entry<String,List<Double>> entry: particijePoZnacajki.entrySet()) {
            result -= entry.getValue().stream().mapToDouble(Double::doubleValue).sum() /sum * entropije.get(entry.getKey());
        }
        return result;
    }

    public List<String> vrijednostiZnacajke(List<List<String>> data, int position) {
        List<String> vrijednostiZnacajke = new ArrayList<>();
        for(List<String> primjer: data) {
            String vrijednostZnacajke = primjer.get(position);
            if (!vrijednostiZnacajke.contains(vrijednostZnacajke)) {
                vrijednostiZnacajke.add(vrijednostZnacajke);
            }
        }
        return vrijednostiZnacajke;
    }

    private Map<String, List<Double>> getParticijePoZnacajki(List<List<String>> data, int position, List<String> znacajke) {
        List<String> ciljneVarijable = vrijednostiZnacajke(data, data.get(0).size()-1);
        Map<String, List<Double>> result = new TreeMap<>();
        String znacajkaKojuTrazim = znacajke.get(position);
        position = znacajkeICilj.indexOf(znacajkaKojuTrazim);

        for (List<String> primjer : data){
            String znacajka = primjer.get(position);
            String ciljnaVarijabla = primjer.get(primjer.size()-1);
            int index = ciljneVarijable.indexOf(ciljnaVarijabla);
            List<Double> tempList = new ArrayList<>();

            if(result.containsKey(znacajka)) {
                tempList = result.get(znacajka);

            } else {
                for(int i = 0; i < ciljneVarijable.size(); i++) {
                    tempList.add(0.0);
                }
            }
            tempList.set(index, tempList.get(index) + 1.0);
            result.put(znacajka, tempList);
        }
        return result;
    }
}
