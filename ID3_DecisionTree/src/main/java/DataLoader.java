import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DataLoader {
    public DataLoader() {}
    public List<List<String>> loadData(Path trainFile) throws IOException {
        Scanner sc = new Scanner(trainFile);
        List<List<String>> data = new ArrayList<>();

        while(sc.hasNext()) {
            data.add(Arrays.stream(sc.nextLine().split(",")).toList());
        }
        return data;
    }
}
