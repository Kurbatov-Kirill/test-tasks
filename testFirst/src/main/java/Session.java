import java.util.*;

public class Session {
    private int tubesAmount;
    private int tubesVolume;
    private int dropsVariety;
    private State currentState = new State();
    List<State> states = new ArrayList<>();
    private final List<int[]> steps = new ArrayList<>();
    private final List<List<int[]>> blockingHistory = new ArrayList<>();

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

                dropsVariety = 12;
                tubesAmount = dropsVariety + 2;
                tubesVolume = 4;

                autolaunchMode = true;
            } else if (inputNum == 2) {
                System.out.print("Введите объём одной пробирки: ");
                int v = input.nextInt();    // объём одной пробирки
                System.out.print("Введите количество видов жидкостей: ");
                int m = input.nextInt();    // количество видов жидкостей

                dropsVariety = m;
                tubesAmount = m + 2;
                tubesVolume = v;
            }
        }
        System.out.print(tubesAmount + ": пробирок\n");
        System.out.print(tubesVolume + ": объём\n");
        System.out.print(dropsVariety + ": видов\n");
        return autolaunchMode;
    }

    private void initStartCondition(boolean autolaunch) {
        if (!autolaunch) {
            Scanner input = new Scanner(System.in);
            System.out.println();
            for (int i = 0; i < dropsVariety; i++) {
                Tube tube = new Tube();
                tube.setVolume(tubesVolume);
                for (int j = 0; j < tubesVolume; j++) {
                    Drop drop = new Drop();
                    System.out.print("Пробирка " + (i + 1) + ", ячейка " + (j + 1) + " (снизу вверх): ");
                    drop.setColorId(input.nextInt());
                    tube.getContents().add(drop);
                }
                currentState.getCurrentState().add(tube);
                System.out.println();
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
        printState("=========");
    }

    private List<Drop> pickDropsToTransfer(int tubeIndex) {
        List<Drop> dropsToTransfer = new ArrayList<>();

        Tube tube = currentState.getCurrentState().get(tubeIndex);
        int tubeSize = tube.getContents().size();

        if ( tube.hasLiquid() && !tube.isSorted() &&
                ( !tube.isContainsOneKindOfLiquid() || (tube.getContents().size() == 1) )
        ) {

            for (int j = tubeSize - 1; j >= 0; j--) {
                Drop dropToPick = tube.getContents().get(j);
                dropsToTransfer.add(dropToPick);
                if ((j - 1) >= 0) {
                    if (tube.getContents().get(j - 1).getColorId() != dropToPick.getColorId()) {    // если предпоследняя капля равна той, что взяли
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return dropsToTransfer;
    }

    private int pickTubeToDeliver(int originalTubeIndex, List<Drop> dropsToTransfer) {
        int destinationTubeIndex = -1;

        for (int i = 0; i < currentState.getCurrentState().size(); i++) {

            Tube tube = currentState.getCurrentState().get(i);
            tube.setVolume(tubesVolume);
            if (!tube.hasLiquid()) {
                destinationTubeIndex = i;
                break;
            } else if (
                    tube.hasFreeSpace() &&
                            (tube.getTopDrop().getColorId() == dropsToTransfer.getLast().getColorId()) &&
                            (i != originalTubeIndex) &&
                            (dropsToTransfer.size() <= tube.getFreeCells())
            ) {
                destinationTubeIndex = i;
                break;
            }
        }
        return destinationTubeIndex;
    }

    private void makeTransfer() {
        for (int k = 0; k < tubesAmount; k++) {
            if (executeTransfer(k)) {
                states.add(new State(currentState));
                return;
            }
        }

        if (!steps.isEmpty() && !states.isEmpty()) {
            rollback();
        } else {
            System.exit(0);
        }
    }

    private void printState(String info) {
        System.out.println("\n======" + info + "======\n");
        for (int i = 0; i < tubesAmount; i++) {
            System.out.print(i + "\t|\t");
            Tube tube = currentState.getCurrentState().get(i);
            for (int j = 0; j < tube.getContents().size(); j++) {
                if (!(tube.getContents().isEmpty())) {
                    System.out.print(tube.getContents().get(j).getColorId() + "\t");
                } else {
                    System.out.println(0);
                }
            }
            System.out.println();
        }
    }

    private boolean executeTransfer(int departureTubeIndex) {
        List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);

        if (!dropsToTransfer.isEmpty()) {
            int destinationTubeIndex = pickTubeToDeliver(departureTubeIndex, dropsToTransfer);

            if (!blockingHistory.isEmpty()) {
                int[] currentStep = new int[]{departureTubeIndex, destinationTubeIndex};
                int currentStepIndex = steps.size() - 1;
                if (!blockingHistory.get(currentStepIndex).isEmpty()) {
                    List<int[]> currentBlockedStatesList;

                    currentBlockedStatesList = blockingHistory.get(currentStepIndex);

                    for (int[] step: currentBlockedStatesList) {
                        if (Arrays.equals(step, currentStep)) {
                            return false;
                        }
                    }
                }
            }

            if (destinationTubeIndex >= 0) {
                Tube tubeToPick = currentState.getCurrentState().get(departureTubeIndex);
                Tube tubeToFill = currentState.getCurrentState().get(destinationTubeIndex);
                List<Drop> modifiedContentsOfOriginalTube = tubeToPick.getContents();
                int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());

                for (int i = 0; i < x; i++) {
                    modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                    tubeToFill.getContents().add(dropsToTransfer.getLast());
                    dropsToTransfer.remove(dropsToTransfer.getLast());
                }

                tubeToPick.setContents(modifiedContentsOfOriginalTube);
                currentState.getCurrentState().set(departureTubeIndex, new Tube(tubeToPick));

                currentState = new State(currentState);
                printState(" " + departureTubeIndex + " >> " + destinationTubeIndex + " ");

                steps.add(new int[]{departureTubeIndex, destinationTubeIndex});

                if (steps.size() != blockingHistory.size()) {
                    blockingHistory.add(new ArrayList<>());
                }

                return true;
            }
        }
        return false;
    }

    private void rollback() {
        List<int[]> currentBlockedSteps;
        int currentStepIndex = steps.size() - 2;

        currentBlockedSteps = blockingHistory.get(currentStepIndex);
        currentBlockedSteps.add(steps.getLast());
        blockingHistory.set(currentStepIndex,currentBlockedSteps);
        steps.removeLast();
        states.removeLast();
        currentState = states.getLast();
    }

    private boolean sortSucceeded() {
        List<Tube> sortedTubes = new ArrayList<>();
        int amountOfSortedTubes;
        for (int i = 0; i < tubesAmount; i++) {
            Tube tube = currentState.getCurrentState().get(i);
            tube.setVolume(tubesVolume);

            if (!(tube.getContents().isEmpty())) {
                if (tube.isSorted()) {
                    sortedTubes.add(tube);
                }
            }
        }
        amountOfSortedTubes = sortedTubes.size();
        return amountOfSortedTubes == dropsVariety;
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
