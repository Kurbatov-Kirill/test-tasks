import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Session {
    private int tubesAmount;
    private int tubesVolume;
    private int dropsVariety;
    private List<Tube> currentState = new ArrayList<>();
    private int[] steps = new int[2];

    /*===========================================*/

    public int getTubesAmount() {
        return tubesAmount;
    }

    public void setTubesAmount(int tubesAmount) {
        this.tubesAmount = tubesAmount;
    }

    public int getTubesVolume() {
        return tubesVolume;
    }

    public void setTubesVolume(int tubesVolume) {
        this.tubesVolume = tubesVolume;
    }

    public int getDropsVariety() {
        return dropsVariety;
    }

    public void setDropsVariety(int dropsVariety) {
        this.dropsVariety = dropsVariety;
    }

    public List<Tube> getCurrentState() {
        return currentState;
    }

    public void setCurrentState(List<Tube> currentState) {
        this.currentState = currentState;
    }

    public int[] getSteps() {
        return steps;
    }

    public void setSteps(int[] steps) {
        this.steps = steps;
    }

    /*===========================================*/

    public void start() {
        receiveInitData();
        initStartCondition();
        pickDropsToTransfer();
        pickTubeToDeliver();
    }

    /*===========================================*/

    private void receiveInitData() {
        Scanner input = new Scanner(System.in);
        /*System.out.print("Введите количество пробирок: ");
        int n = input.nextInt();    // количество пробирок*/
        System.out.print("Введите объём одной пробирки: ");
        int v = input.nextInt();    // объём одной пробирки
        System.out.print("Введите количество видов жидкостей: ");
        int m = input.nextInt();    // количество видов жидкостей

        setTubesAmount(m + 2);
        setTubesVolume(v);
        setDropsVariety(m);

        System.out.print(getTubesAmount() + ": пробирок\n");
        System.out.print(getTubesVolume() + ": объём\n");
        System.out.print(getDropsVariety() + ": видов\n");
    }

    private void initStartCondition() {

        /*int number = tubesAmount;
        int volume = tubesVolume;
        int species = dropsVariety;*/

        Scanner input = new Scanner(System.in);

        //currentState = new int[number][volume];
        for (int i = 0; i < dropsVariety; i++) {
            Tube tube = new Tube();
            for (int j = 0; j < tubesVolume; j++) {
                Drop drop = new Drop();
                System.out.print("Введите код жидкости для заполнения пробирки " + (i + 1) + ", уровня " + (j + 1) + ": ");
                drop.setColorId(input.nextInt());
                tube.getContents().add(drop);
            }
            getCurrentState().add(tube);


        }
        for (int i = dropsVariety; i < tubesAmount; i++) {
            Tube tube = new Tube();
            for (int j = 0; j < tubesVolume; j++) {
                Drop drop = new Drop();
                drop.setColorId(0);
                tube.getContents().add(drop);
            }

            getCurrentState().add(tube);
            System.out.println();
        }
        input.close();
        for (int i = 0; i < tubesAmount; i++) {
            for (int j = 0; j < tubesVolume; j++) {
                System.out.print(getCurrentState().get(i).getContents().get(j).getColorId() + "\t");
            }
            System.out.println();
        }
    }

    private List<Drop> pickDropsToTransfer() {
        List<Drop> dropsToTransfer = new ArrayList<>();

        outer:
        for (int i = 0; i < getCurrentState().size(); i++) {
            Tube tube = getCurrentState().get(i);
            int tubeSize = tube.getContents().size();
            if (tube.hasLiquid()) {
                for (int j = tubeSize - 1; j >= 0; j--) {
                    Drop dropToPick = tube.getContents().get(j);
                    dropsToTransfer.add(dropToPick);
                    if (tube.getContents().get(tubeSize - j).getColorId() != dropToPick.getColorId()) {    // если предпоследняя капля равна той, что взяли
                        break outer;
                    }
                }
            }
        }
        return dropsToTransfer;
    }

    private void pickTubeToDeliver() {
        // TODO: выбор пробирки, куда будем переливать - если пустая, то сразу подходит и берём,
        //  если не пустая - есть ли свободное место + подходит ли верхняя капля по цвету к той/тем, что хотим добавить
    }
}
