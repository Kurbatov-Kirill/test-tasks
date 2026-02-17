import java.util.*;

public class Session {
    private int tubesAmount;
    private int tubesVolume;
    private int dropsVariety;
    private State currentState = new State();
    List<State> states = new ArrayList<>();
    private List<int[]> steps = new ArrayList<>();
    private List<int[]> blockingSteps = new ArrayList<>();
    private int tempOriginalTubeId;
    private int destinationTubeIndex;
    private enum priorityMode {FILL, PICK, NONE};
    private priorityMode priority = priorityMode.NONE;
    private List<List<int[]>> blockingHistory = new ArrayList<>();

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

    public State getCurrentState() {
        return currentState;
    }

    public int getTempOriginalTubeId() {
        return tempOriginalTubeId;
    }

    public void setTempOriginalTubeId(int tempOriginalTubeId) {
        this.tempOriginalTubeId = tempOriginalTubeId;
    }

    public int getDestinationTubeIndex() {
        return destinationTubeIndex;
    }
    /*===========================================*/

    public void start() {
        initStartCondition(receiveInitData());
        while (!sortSucceeded()) {
            makeTransfer();
        }
        printSuccess();
    }
    /*===========================================*/

    private boolean receiveInitData() {
        boolean autolaunchMode = false;
        Scanner input = new Scanner(System.in);

        System.out.println("Использовать условия из задачи? (12 цветов, пробирки вмещают 4 \"капли\")");
        System.out.println("1: Да");
        System.out.println("2: Нет");

        if (input.hasNextInt()) {
            int inputNum = input.nextInt();
            if (inputNum == 1) {
                int[] tubesContentByDefault = {
                        4, 4, 10, 2,
                        8, 12, 8, 1,
                        9, 5, 7, 10,
                        5, 2, 3, 5,
                        7, 8, 11, 6,
                        2, 1, 12, 12,
                        11, 8, 7, 4,
                        1, 3, 11, 10,
                        9, 9, 7, 10,
                        11, 6, 2, 6,
                        3, 9, 6, 4,
                        1, 12, 3, 5
                };
                dropsVariety = 12;
                tubesVolume = 4;
                for (int i = 0; i < dropsVariety; i++) {
                    Tube tube = new Tube();
                    tube.setVolume(tubesVolume);
                    for (int j = 0; j < tubesVolume; j++) {
                        Drop drop = new Drop();
                        drop.setColorId(tubesContentByDefault[(i * 4) + j]);
                        tube.getContents().add(drop);
                    }
                    currentState.getCurrentState().add(tube);
                }
                setTubesAmount(12 + 2);
                setTubesVolume(4);
                setDropsVariety(12);
                autolaunchMode = true;
            } else if (inputNum == 2) {
                System.out.print("Введите объём одной пробирки: ");
                int v = input.nextInt();    // объём одной пробирки
                System.out.print("Введите количество видов жидкостей: ");
                int m = input.nextInt();    // количество видов жидкостей

                setTubesAmount(m + 2);
                setTubesVolume(v);
                setDropsVariety(m);
            }
        }
        System.out.print(getTubesAmount() + ": пробирок\n");
        System.out.print(getTubesVolume() + ": объём\n");
        System.out.print(getDropsVariety() + ": видов\n");
        return autolaunchMode;
    }

    private void initStartCondition(boolean autolaunch) {
        if (!autolaunch) {
            Scanner input = new Scanner(System.in);
            for (int i = 0; i < dropsVariety; i++) {
                Tube tube = new Tube();
                tube.setVolume(tubesVolume);
                for (int j = 0; j < tubesVolume; j++) {
                    Drop drop = new Drop();
                    System.out.print("Введите код жидкости для заполнения пробирки " + (i + 1) + ", уровня " + (j + 1) + ": ");
                    drop.setColorId(input.nextInt());
                    tube.getContents().add(drop);
                }
                currentState.getCurrentState().add(tube);
            }
            input.close();
        }

        for (int i = dropsVariety; i < tubesAmount; i++) {
            Tube tube = new Tube();
            tube.setVolume(tubesVolume);
            currentState.getCurrentState().add(tube);
            System.out.println();
        }
        states.add(new State(currentState));
        printState();
    }

    private List<Drop> pickDropsToTransfer(int tubeIndex) {
        List<Drop> dropsToTransfer = new ArrayList<>();
        System.out.println("BASE: " + tubeIndex + " \n");

        Tube tube = currentState.getCurrentState().get(tubeIndex);
        int tubeSize = tube.getContents().size();

        setTempOriginalTubeId(tubeIndex);

        if ( tube.hasLiquid() && !tube.isSorted() && (!tube.isContainsOneKindOfLiquid() || (tube.getContents().size() == 1)) ) {
            System.out.println("picked_TUBE: " + tubeIndex + ", HAS LIQUID: " + tube.hasLiquid() + " 1KIND: " + tube.isContainsOneKindOfLiquid() + " 1DROP: " + (tube.getContents().size() == 1) + "\n");

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

            int currentColor = dropsToTransfer.getLast().getColorId();
            for (int i = 0; i < currentState.getCurrentState().size(); i++) {

                Tube tube = currentState.getCurrentState().get(i);
                tube.setVolume(tubesVolume);
                int tubeSize = tube.getContents().size();
                if (!tube.hasLiquid()) {
                    destinationTubeIndex = i;
                    //break;
                } else if (tube.hasFreeSpace() && (tube.getTopDrop().getColorId() == dropsToTransfer.getLast().getColorId()) && (i != getTempOriginalTubeId()) && (dropsToTransfer.size() <= tube.getFreeCells())) {
                    destinationTubeIndex = i;
                    break;
                }
            }
        System.out.print("tubeToFill: " + destinationTubeIndex + "\n");
        return destinationTubeIndex;
    }

    private void prepareToTransfer(int tubeWithPriorityToFill, int tubeWithPriorityToPick) {
        if (tubeWithPriorityToFill >= 0 && (priority == priorityMode.FILL)) {
            if (prepareForFillTransfer(tubeWithPriorityToFill)) {
                System.out.println("FILL\n");
            } else if (tubeWithPriorityToPick >= 0 && (priority == priorityMode.PICK)) {
                if (executeTransfer(tubeWithPriorityToPick)) {
                    System.out.println("PICK\n");
                } else {
                    for (int k = 0; k < tubesAmount; k++) {
                        if (executeTransfer(k)) {
                            states.add(new State(currentState));
                            System.out.println(states.size());
                            blockingSteps.clear();
                            return;
                        }
                    }
                    System.out.println("PICK FAILED\n");
                }
            } else {
                for (int k = 0; k < tubesAmount; k++) {
                    if (executeTransfer(k)) {
                        states.add(new State(currentState));
                        System.out.println(states.size());
                        blockingSteps.clear();
                        return;
                    }
                }
                System.out.println("FILL FAILED\n");
            }
        } else if (tubeWithPriorityToPick >= 0 && (priority == priorityMode.PICK)) {
            if (executeTransfer(tubeWithPriorityToPick)) {
                System.out.println("PICK\n");
            } else {
                for (int k = 0; k < tubesAmount; k++) {
                    if (executeTransfer(k)) {
                        states.add(new State(currentState));
                        System.out.println(states.size());
                        blockingSteps.clear();
                        return;
                    }
                }
                System.out.println("PICK FAILED\n");
            }
        } else {
            for (int k = 0; k < tubesAmount; k++) {
                if (executeTransfer(k)) {
                    states.add(new State(currentState));
                    System.out.println(states.size());
                    blockingSteps.clear();
                    return;
                }
            }
            System.out.println("BASIC FAILED\n");
        }
        System.out.println("FAIL\n");
        if (destinationTubeIndex < 0) {
            if (!steps.isEmpty() && !states.isEmpty()) {
                rollback();
                //printBlocked();
            } else {
                System.out.println("FAILURE");
                System.exit(0);
            }
        }
    }

    private void makeTransfer() {
        int tubeWithPriorityToFill = tubeWithPriorityToFill();
        int tubeWithPriorityToPick = tubeWithPriorityToPick();
        //int tubeWithPriority = tubeWithPriority(tubeWithPriorityToFill, tubeWithPriorityToPick);

        //prepareToTransfer(tubeWithPriority, priority);
        prepareToTransfer(tubeWithPriorityToFill, tubeWithPriorityToPick);
        printResults();
    }

    private void printState() {
        for (int i = 0; i < tubesAmount; i++) {
            Tube tube = currentState.getCurrentState().get(i);
            for (int j = 0; j < tube.getContents().size(); j++) {
                if (!(currentState.getCurrentState().get(i).getContents().size() == 0)) {
                    System.out.print(currentState.getCurrentState().get(i).getContents().get(j).getColorId() + "\t");
                } else {
                    System.out.print(0 + "\t");
                }
            }
            System.out.println();
        }
    }

    private boolean executeTransfer(int departureTubeIndex) {
        List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);
        System.out.println("makeTransferDrops: " + dropsToTransfer + "\n");

        if (!dropsToTransfer.isEmpty()) {
            if (!lastStepDuplicated(pickTubeToDeliver(dropsToTransfer))) {
                int destinationTubeIndex = pickTubeToDeliver(dropsToTransfer);
                System.out.println("TUBE_ID: " + destinationTubeIndex + "\n");

                if (!blockingSteps.isEmpty()) {
                    System.out.println("CHECK\n");
                    int[] currentStep = new int[]{departureTubeIndex, destinationTubeIndex};
                    System.out.println("CURRENT STEP: " + Arrays.toString(currentStep) + "\n");
                    System.out.println("LAST BLOCKING STEP: " + Arrays.toString(blockingSteps.getLast()) + "\n");
                    int currentStepIndex = steps.size() - 1;
                    List<int[]> currentBlockedStatesList;
                    currentBlockedStatesList = blockingHistory.get(currentStepIndex);
                    for (int[] step: currentBlockedStatesList) {
                        if (Arrays.equals(step, currentStep)) {
                            System.out.println("BLOCK PREVENTION\n");
                        /*if (!steps.isEmpty() && !states.isEmpty()) {
                            rollback();
                            printBlocked();

                        }*/
                            return false;
                        }
                    }
                }

                if (destinationTubeIndex >= 0) {
                    Tube tubeToFill = currentState.getCurrentState().get(destinationTubeIndex);
                    List<Drop> modifiedContentsOfOriginalTube = currentState.getCurrentState().get(getTempOriginalTubeId()).getContents();
                    int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());
                    for (int i = 0; i < x; i++) {
                        modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                        tubeToFill.getContents().add(dropsToTransfer.getLast());
                        dropsToTransfer.remove(dropsToTransfer.getLast());
                    }
                    currentState.getCurrentState().get(departureTubeIndex).setContents(modifiedContentsOfOriginalTube);
                    Tube modifiedTube = currentState.getCurrentState().get(departureTubeIndex);
                    currentState.getCurrentState().set(getTempOriginalTubeId(), new Tube(modifiedTube));
                    //currentState.getCurrentState().add(new Tube(modifiedTube));
                    currentState = new State(currentState);
                    printState();
                    System.out.println("TRANSFER OK");
                    steps.add(new int[]{getTempOriginalTubeId(), destinationTubeIndex});
                    if (steps.size() != blockingHistory.size()) {
                        blockingHistory.add(new ArrayList<>());
                    }

                    return true;
                }
            } else {
                System.out.println("DUPLICATED\n");
            }
        }
        return false;
    }

    private void rollback() {
        printResults();
        System.out.println("ROLLBACK");

        List<int[]> currentBlockedSteps = new ArrayList<>();


        blockingSteps.add(steps.getLast());

        int currentStepIndex = steps.size() - 2;
        currentBlockedSteps = blockingHistory.get(currentStepIndex);
        currentBlockedSteps.add(steps.getLast());
        blockingHistory.set(currentStepIndex,currentBlockedSteps);
        //blockingHistory.removeLast();
        steps.removeLast();
        System.out.println("STATES: " + states.getLast().toString());
        states.removeLast();
        currentState = states.getLast();


        printResults();
        printState();

    }

    private boolean executeTransfer(int departureTubeIndex, int destinationTubeIndex) {
        if (departureTubeIndex > 0) {
            List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);
            System.out.println("makeTransferDrops: " + dropsToTransfer + "\n");

            if (!dropsToTransfer.isEmpty()) {
                if (!lastStepDuplicated(pickTubeToDeliver(dropsToTransfer))) {
                    System.out.println("TUBE_ID: " + destinationTubeIndex + "\n");

                    if (!blockingSteps.isEmpty()) {
                        System.out.println("CHECK\n");
                        int[] currentStep = new int[]{departureTubeIndex, destinationTubeIndex};
                        System.out.println("CURRENT STEP: " + Arrays.toString(currentStep) + "\n");
                        System.out.println("LAST BLOCKING STEP: " + Arrays.toString(blockingSteps.getLast()) + "\n");
                        int currentStepIndex = steps.size();
                        List<int[]> currentBlockedStatesList;
                        currentBlockedStatesList = blockingHistory.get(currentStepIndex);
                        for (int[] step: currentBlockedStatesList) {
                            if (Arrays.equals(step, currentStep)) {
                                System.out.println("BLOCK PREVENTION\n");
                        /*if (!steps.isEmpty() && !states.isEmpty()) {
                            rollback();
                            printBlocked();

                        }*/
                                return false;
                            }
                        }
                    }

                    if (!(destinationTubeIndex < 0)) {
                        Tube tubeToFill = currentState.getCurrentState().get(destinationTubeIndex);
                        List<Drop> modifiedContentsOfOriginalTube = currentState.getCurrentState().get(departureTubeIndex).getContents();
                        int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());
                        for (int i = 0; i < x; i++) {
                            modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                            tubeToFill.getContents().add(dropsToTransfer.getLast());
                            dropsToTransfer.remove(dropsToTransfer.getLast());
                        }
                        Tube modifiedTube = currentState.getCurrentState().get(departureTubeIndex);
                        currentState.getCurrentState().set(getTempOriginalTubeId(), new Tube(modifiedTube));
                        //currentState.getCurrentState().add(new Tube(modifiedTube));
                        currentState = new State(currentState);
                        System.out.println("TRANSFER OK");
                        printState();
                        steps.add(new int[]{departureTubeIndex, destinationTubeIndex});
                        blockingHistory.add(new ArrayList<>());
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
            Tube tube = currentState.getCurrentState().get(i);
            tube.setVolume(tubesVolume);

            if (!(tube.getContents().size() == 0)) {
                if (tube.isSorted()) {
                    sortedTubes.add(tube);
                }
            }
        }
        amountOfSortedTubes = sortedTubes.size();
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

    /*private int tubeWithPriority(int tubeWithPriorityToFill, int tubeWithPriorityToPick) {
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
    }*/

    private int tubeWithPriorityToFill () {
        int tubeWithPriority = -1;
        for (int i = 0; i < currentState.getCurrentState().size(); i++) {

            Tube tube = currentState.getCurrentState().get(i);
            int tubeFilledSize = tube.getContents().size();
        if (tube.hasLiquid() && (tubeFilledSize < tube.getVolume()) && tube.isContainsOneKindOfLiquid()) {
                tubeWithPriority = i;
                break;
            }
        }
        return tubeWithPriority;
    }

    private int tubeWithPriorityToPick() {
        int tubeWithPriority = -1;
        //for (int i = currentState.getCurrentState().size() - 1; i >= 0; i--) {
        for (int i = 0; i < currentState.getCurrentState().size(); i++) {

            Tube tube = currentState.getCurrentState().get(i);
            int tubeFilledSize = tube.getContents().size();
            if (tubeFilledSize == 1) {
                tubeWithPriority = i;
                break;
            }
        }
        return tubeWithPriority;
    }

    private boolean prepareForFillTransfer(int destinationTubeIndex) {
        int departureTubeIndex = -1;

        Tube tube = currentState.getCurrentState().get(destinationTubeIndex);
        int topDropColor = tube.getTopDrop().getColorId();

        for (int i = 0; i < currentState.getCurrentState().size(); i++) {
            tube = currentState.getCurrentState().get(i);
            if (!tube.getContents().isEmpty() && (i != destinationTubeIndex) && (topDropColor == tube.getTopDrop().getColorId())) {
                departureTubeIndex = i;
                break;
            }
        }
        return executeTransfer(departureTubeIndex, destinationTubeIndex);
    }

    private void printResults() {
        for (int[] step : steps) {
            System.out.print("(" + step[0]);
            System.out.print(", ");
            System.out.print(step[1]);
            System.out.print("), ");
        }
        System.out.println("\n");
    }

    private void printSuccess() {
        System.out.println("Решение найдено!");
        System.out.println("Ходов: " + steps.size());
        for (int[] step : steps) {
            System.out.print("(" + step[0]);
            System.out.print(", ");
            System.out.print(step[1]);
            System.out.print("), ");
        }
        System.out.println("\n");
    }
}
