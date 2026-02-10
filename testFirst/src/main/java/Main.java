import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        Main program = new Main();

        int[] initData = program.receiveInitData();
        int[][] currentField = program.initStartCondition(initData);
        program.trySort(currentField);
    }

    private int[] receiveInitData() {
        Scanner input = new Scanner(System.in);
        System.out.print("Введите количество пробирок: ");
        int n = input.nextInt();    // количество пробирок
        System.out.print("Введите объём одной пробирки: ");
        int v = input.nextInt();    // объём одной пробирки
        System.out.print("Введите количество видов жидкостей: ");
        int m = input.nextInt();    // количество видов жидкостей

        System.out.print(n + ": пробирок\n");
        System.out.print(n + ": объём\n");
        System.out.print(n + ": видов\n");

        return new int[]{n,v,m};
    }

    private int[][] initStartCondition(int[] initData) {

        int number = initData[0];
        int volume = initData[1];
        int species = initData[2];

        Scanner input = new Scanner(System.in);

        int[][] tubes = new int[number][volume];
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < volume; j++) {
                System.out.print("Enter element at row " + (i + 1) + ", column " + (j + 1) + ": ");
                tubes[i][j] = input.nextInt();
                System.out.print(tubes[i][j] + "\t");
            }
            System.out.println();
        }
        input.close();
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < volume; j++) {
                System.out.print(tubes[i][j] + "\t");
            }
            System.out.println();
        }
        return tubes;
    }

    private void trySort(int[][] tubes) {
        int tempValToTransfer;
        int tempValOfNeighbour;
        int tempDeparture[] = new int[2];
        int tempDestination[] = new int[2];
        outerLoop:
        for (int row = 0; row < tubes.length; row++) {  // пробирка
            if (!IntStream.of(tubes[row]).allMatch(n -> n == 0)) {
                for (int col = tubes[row].length - 1; col >= 0; col--) { // слой пробирки
                    if(tubes[row][col] != 0) {
                        tempValToTransfer = tubes[row][col];
                        tempDeparture[0] = row;
                        tempDeparture[1] = col;
                        break outerLoop;
                    }
                    System.out.print(tubes[row][col] + "\t");
                }
                System.out.println();
            }
        }

        outerLoop:
        for (int row = 0; row < tubes.length; row++) {  // пробирка
            if (Arrays.asList(tubes[row]).contains(0)) {
                for (int col = 0; col < tubes[row].length; col++) { // слой пробирки
                    if(tubes[row][col] == 0) {
                        tempDestination[0] = row;
                        tempDestination[1] = col;

                        break outerLoop;
                    }
                    System.out.print(tubes[row][col] + "\t");
                }
                System.out.println();
            }
        }


    }

    private void destCheckForEmptyRows(int[][] tubes) {
        outerLoop:
        for (int row = 0; row < tubes.length; row++) {  // пробирка
            if (IntStream.of(tubes[row]).allMatch(n -> n == 0)) {
                for (int col = tubes[row].length - 1; col >= 0; col--) { // слой пробирки
                    if(tubes[row][col] != 0) {
                        /*tempValToTransfer = tubes[row][col];
                        tempDeparture[0] = row;
                        tempDeparture[1] = col;*/
                        break outerLoop;
                    }
                    System.out.print(tubes[row][col] + "\t");
                }
                System.out.println();
            }
        }
    }
}
