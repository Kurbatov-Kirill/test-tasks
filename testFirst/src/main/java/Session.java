import java.util.*;

public class Session {
    private int tubesAmount;
    private int tubesVolume;
    private int dropsVariety;
    private List<Tube> currentState = new ArrayList<>();
    List<List<Tube>> states = new ArrayList<>();
    private List<int[]> steps = new ArrayList<>();
    private int tempOriginalTubeId;
    private int destinationTubeIndex;
    private enum priorityMode {FILL, PICK, NONE};
    private priorityMode priority = priorityMode.NONE;
    //private int priorityTubeIndex;

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

    public List<int[]> getSteps() {
        return steps;
    }

    public int getTempOriginalTubeId() {
        return tempOriginalTubeId;
    }

    public void setTempOriginalTubeId(int tempOriginalTubeId) {
        this.tempOriginalTubeId = tempOriginalTubeId;
    }

    public void setSteps(List<int[]> steps) {
        this.steps = steps;
    }

    public int getDestinationTubeIndex() {
        return destinationTubeIndex;
    }

    public void setDestinationTubeIndex(int destinationTubeIndex) {
        this.destinationTubeIndex = destinationTubeIndex;
    }
    /*===========================================*/

    public void start() {
        receiveInitData();
        initStartCondition();

        while (!sortSucceeded()) {
            makeTransfer();
            //System.out.print("Sort succeeded: " + sortSucceeded());
        }
        printResults();
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
            tube.setVolume(tubesVolume);
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
            tube.setVolume(tubesVolume);
            /*for (int j = 0; j < tubesVolume; j++) {
                Drop drop = new Drop();
                drop.setColorId(0);
                tube.getContents().add(drop);
            }*/

            getCurrentState().add(tube);
            System.out.println();
        }
        input.close();
        for (int i = 0; i < tubesAmount; i++) {
            Tube tube = getCurrentState().get(i);
            for (int j = 0; j < tube.getContents().size(); j++) {
                if (!(getCurrentState().get(i).getContents().size() == 0)) {
                    System.out.print(getCurrentState().get(i).getContents().get(j).getColorId() + "\t");
                } else {
                    System.out.print(0 + "\t");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private List<Drop> pickDropsToTransfer(int tubeIndex) {
        List<Drop> dropsToTransfer = new ArrayList<>();
        System.out.println("BASE: " + tubeIndex + " \n");
        /*if (currentState.get(tubeIndex).hasLiquid()) {
            int topDropColor = currentState.get(tubeIndex).getTopDrop().getColorId();
            for (int i = currentState.size() - 1; i > 0; i--) {
                if (currentState.get(i).hasLiquid()) {
                    if (currentState.get(i).getTopDrop().getColorId() == topDropColor) {
                        //setTempOriginalTubeId(i);
                        tubeIndex = i;
                        break;
                    }
                }
            }
        }*/

        Tube tube = getCurrentState().get(tubeIndex);
        int tubeSize = tube.getContents().size();

        setTempOriginalTubeId(tubeIndex);

        if (tube.hasLiquid() && !tube.isSorted()) {
            System.out.println("picked_TUBE: " + tubeIndex + ", HAS LIQUID: " + tube.hasLiquid() + " 1KIND: " + tube.isContainsOneKindOfLiquid() + " 1DROP: " + (tube.getContents().size() == 1) + "\n");

            //setTempOriginalTubeId(tubeIndex);


            for (int j = tubeSize - 1; j >= 0; j--) {
                Drop dropToPick = tube.getContents().get(j);
                dropsToTransfer.add(dropToPick);
                if ((j - 1) >= 0) {
                    if ((tube.getContents().get(j - 1).getColorId() != dropToPick.getColorId())) {    // если предпоследняя капля равна той, что взяли
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        System.out.println("drops to transfer: " + dropsToTransfer + " \n");
        return dropsToTransfer;
    }

    private int pickTubeToDeliver(List<Drop> dropsToTransfer) {
        destinationTubeIndex = -1;
        System.out.println("DROPs: " + dropsToTransfer + "\n");

        //if (destinationTubeIndex < 0) {
            int currentColor = dropsToTransfer.getLast().getColorId();
            //for (int i = 0; i < getCurrentState().size(); i++) {
            for (int i = 0; i < getCurrentState().size(); i++) {

                Tube tube = getCurrentState().get(i);
                tube.setVolume(tubesVolume);
                //System.out.print("Tube " + i + " has free space: " + tube.hasFreeSpace() + "\n");
                //System.out.print("Tube " + i + " has liquid: " + tube.hasLiquid() + "\n");
                int tubeSize = tube.getContents().size();
                /*if (i == currentState.get(tubeWithPriorityToFill()).getTopDrop().getColorId()) {
                    destinationTubeIndex = i;
                    break;
                }
                else */if (!tube.hasLiquid()) {
                    destinationTubeIndex = i;
                    //System.out.print("tubeToFillSet: " + destinationTubeIndex + "\n");
                    break;
                } else if (tube.hasFreeSpace() && (tube.getTopDrop().getColorId() == dropsToTransfer.getLast().getColorId()) && (i != getTempOriginalTubeId())) {
                    destinationTubeIndex = i;
                    //System.out.print("tubeToFillSet2: " + tube.hasFreeSpace() + "\n");
                    break;
                }
            }
        //}
        System.out.print("tubeToFill: " + destinationTubeIndex + "\n");
        return destinationTubeIndex;
    }

    private void makeTransfer() {
        /*int tubeWithPriorityToFill = tubeWithPriorityToFill();
        int tubeWithPriorityToPick = tubeWithPriorityToPick();*/
        int tubeWithPriority = tubeWithPriority(tubeWithPriorityToFill(), tubeWithPriorityToPick());

        if (tubeWithPriority >= 0 && (priority == priorityMode.FILL)){
            if (prepareForFillTransfer(tubeWithPriority)) {
                System.out.println("FILL\n");
            } else {
                for (int k = 0; k < tubesAmount; k++) {
                    if (executeTransfer(k)){
                        break;
                    }
                }
                System.out.println("NONE\n");
            }
        } else if (tubeWithPriority >= 0 && (priority == priorityMode.PICK)){
            if (executeTransfer(tubeWithPriority)){
                System.out.println("PICK\n");
            } else {
                for (int k = 0; k < tubesAmount; k++) {
                    if (executeTransfer(k)){
                        break;
                    }
                }
                System.out.println("NONE\n");
            }
        } else {
            for (int k = 0; k < tubesAmount; k++) {
                if (executeTransfer(k)){
                    break;
                }
            }
            System.out.println("NONE\n");
        }

        if (destinationTubeIndex < 0) {
            System.out.println("FAILURE");
            System.exit(0);
        }
        printResults();
    }

    private boolean executeTransfer(int departureTubeIndex) {
        List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);
        System.out.println("makeTransferDrops: " + dropsToTransfer + "\n");

        if (!dropsToTransfer.isEmpty()) {
            if (!lastStepDuplicated(pickTubeToDeliver(dropsToTransfer))) {
                int destinationTubeId = pickTubeToDeliver(dropsToTransfer);
                System.out.println("TUBE_ID: " + destinationTubeId + "\n");


                if (!(destinationTubeId < 0)) {
                    Tube tubeToFill = getCurrentState().get(destinationTubeId);
                    List<Drop> modifiedContentsOfOriginalTube = getCurrentState().get(getTempOriginalTubeId()).getContents();
                    int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());
                    //System.out.print("OrigModif: " + dropsToTransfer.size());
                    for (int i = 0; i < x; i++) {
                        modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                        //System.out.print("OrigModif: " + modifiedContentsOfOriginalTube);
                        tubeToFill.getContents().add(dropsToTransfer.getLast());
                        dropsToTransfer.remove(dropsToTransfer.getLast());
                    }
                    currentState.get(getTempOriginalTubeId()).setContents(modifiedContentsOfOriginalTube);

                    for (int i = 0; i < tubesAmount; i++) {
                        Tube tube = getCurrentState().get(i);
                        for (int j = 0; j < tube.getContents().size(); j++) {
                            if (!(getCurrentState().get(i).getContents().size() == 0)) {
                                System.out.print(getCurrentState().get(i).getContents().get(j).getColorId() + "\t");
                            } else {
                                System.out.print(0 + "\t");
                            }
                        }
                        System.out.println();
                    }
                    steps.add(new int[]{getTempOriginalTubeId(), getDestinationTubeIndex()});
                    //states.add(currentState);
                    return true;
                }
            } else {
                System.out.println("DUPLICATED\n");
            }
        }
        return false;
    }

    private boolean executeTransfer(int departureTubeIndex, int destinationTubeIndex) {
        if (departureTubeIndex > 0) {
            List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);
            System.out.println("makeTransferDrops: " + dropsToTransfer + "\n");

            if (!dropsToTransfer.isEmpty()) {
                if (!lastStepDuplicated(pickTubeToDeliver(dropsToTransfer))) {
                    System.out.println("TUBE_ID: " + destinationTubeIndex + "\n");

                    if (!(destinationTubeIndex < 0)) {
                        Tube tubeToFill = getCurrentState().get(destinationTubeIndex);
                        List<Drop> modifiedContentsOfOriginalTube = getCurrentState().get(departureTubeIndex).getContents();
                        int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());
                        //System.out.print("OrigModif: " + dropsToTransfer.size());
                        for (int i = 0; i < x; i++) {
                            modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                            //System.out.print("OrigModif: " + modifiedContentsOfOriginalTube);
                            tubeToFill.getContents().add(dropsToTransfer.getLast());
                            dropsToTransfer.remove(dropsToTransfer.getLast());
                        }
                        currentState.get(departureTubeIndex).setContents(modifiedContentsOfOriginalTube);

                        for (int i = 0; i < tubesAmount; i++) {
                            Tube tube = getCurrentState().get(i);
                            for (int j = 0; j < tube.getContents().size(); j++) {
                                if (!(getCurrentState().get(i).getContents().size() == 0)) {
                                    System.out.print(getCurrentState().get(i).getContents().get(j).getColorId() + "\t");
                                } else {
                                    System.out.print(0 + "\t");
                                }
                            }
                            System.out.println();
                        }
                        steps.add(new int[]{departureTubeIndex, destinationTubeIndex});
                        return true;
                    }
                } else {
                    System.out.println("DUPLICATED\n");
                }
            }
        }
        return false;
    }

    private boolean sortSucceeded() {
        List<Tube> sortedTubes = new ArrayList<>();
        int amountOfSortedTubes = 0;
        for (int i = 0; i < tubesAmount; i++) {
            Tube tube = getCurrentState().get(i);
            tube.setVolume(tubesVolume);

            if (!(tube.getContents().size() == 0)) {
                //System.out.print("ABOBA " + tube.isSorted());
                if (tube.isSorted()) {
                    sortedTubes.add(tube);
                }
            }
        }
        amountOfSortedTubes = sortedTubes.size();
        //System.out.print("ABOBA " + amountOfSortedTubes);
        return amountOfSortedTubes == dropsVariety;
    }

    private boolean lastStepDuplicated(int currentDestination) {
        boolean duplicated = false;
        int[] lastStep;

        if (!steps.isEmpty()) {
            lastStep = steps.getLast();
            if ((lastStep[0] == currentDestination) && (tempOriginalTubeId == lastStep[1])) {
                duplicated = true;
            }
        }
        return duplicated;
    }

    private int tubeWithPriorityToFill () { // мб крашнется без проверки не пустая ли пробирка
        int tubeWithPriority = -1;
        //for (int i = getCurrentState().size() - 1; i >= 0; i--) {
        for (int i = 0; i < getCurrentState().size(); i++) {

            Tube tube = getCurrentState().get(i);
            int tubeFilledSize = tube.getContents().size();
        if (tube.hasLiquid() && (tubeFilledSize < tube.getVolume()) && tube.isContainsOneKindOfLiquid()) {
                tubeWithPriority = i;
                priority = priorityMode.FILL;
                break;
            }
        }
        return tubeWithPriority;
    }

    private int tubeWithPriorityToPick() {
        int tubeWithPriority = -1;
        for (int i = getCurrentState().size() - 1; i >= 0; i--) {
        //for (int i = 0; i < getCurrentState().size(); i++) {

            Tube tube = getCurrentState().get(i);
            int tubeFilledSize = tube.getContents().size();
            if (tubeFilledSize == 1) {
                tubeWithPriority = i;
                priority = priorityMode.PICK;
                break;
            }
        }
        return tubeWithPriority;
    }

    private int tubeWithPriority(int tubeWithPriorityToFill, int tubeWithPriorityToPick) {
        int tubeWithPriority;
        if (tubeWithPriorityToFill + tubeWithPriorityToPick < 13) {
            tubeWithPriority = tubeWithPriorityToFill;
            priority = priorityMode.FILL;
        } else if (tubeWithPriorityToFill + tubeWithPriorityToPick > 13) {
            tubeWithPriority = tubeWithPriorityToPick;
            priority = priorityMode.PICK;
        }
        else {
            tubeWithPriority = tubeWithPriorityToFill;
            priority = priorityMode.FILL;
        }
        return tubeWithPriority;
    }

    private boolean prepareForFillTransfer(int destinationTubeIndex) {
        int departureTubeIndex = -1;

        Tube tube = currentState.get(destinationTubeIndex);
        int topDropColor = tube.getTopDrop().getColorId();

        for (int i = 0; i < currentState.size(); i++) {
            tube = currentState.get(i);
            if (!tube.getContents().isEmpty() && (i != destinationTubeIndex) && (topDropColor == tube.getTopDrop().getColorId())) {
                departureTubeIndex = i;
                break;
            }
        }
        return executeTransfer(departureTubeIndex, destinationTubeIndex);
    }

    /*private void prepareForPickTransfer(int departureTubeIndex) {
        int destinationTubeIndex = -1;

        pickDropsToTransfer();
    }*/

    private void printResults() {
        for (int[] step : steps) {
            System.out.print("(" + step[0]);
            System.out.print(", ");
            System.out.print(step[1]);
            System.out.print("), ");
        }
        System.out.println("\n");
    }
}
