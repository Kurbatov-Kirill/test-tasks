package com.github.kurbatov.sorter.classes;

import java.util.*;

// Класс с основной логикой
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
        initStartCondition(receiveInitData());  // инициализируем начальное состояние, исходя из введенных данных
        while (!sortSucceeded()) {              // пытаемся найти решение, пока не отсортируем
            makeTransfer();
        }
        printSuccess();                         // в случае успеха печатаем итоговую последовательност ходов
    }
    /*===========================================*/
    // Получаем данные для построения начального состояния
    private boolean receiveInitData() {
        boolean autolaunchMode = false;
        Scanner input = new Scanner(System.in);

        System.out.println("Использовать условия из задачи? (12 цветов, пробирки вмещают 4 \"капли\")");
        System.out.println("1: Да");
        System.out.println("2: Нет");

        //  Если выбрано автозаполнение (для экономии времени проверки)
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
                //  Если выбран ручной ввод
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
        //  Вывод справки по начальному состоянию
        System.out.print(tubesAmount + ": пробирок\n");
        System.out.print(tubesVolume + ": объём\n");
        System.out.print(dropsVariety + ": видов\n");
        return autolaunchMode;
    }

    //  Обработка введённых данных, создание и заполнение пробирок
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

    // Пытаемся получить верхнюю/верхние капли из пробирки
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

    // Смотрим, куда выбранные капли могут быть перелиты
    private int pickTubeToDeliver(int originalTubeIndex, List<Drop> dropsToTransfer) {
        int destinationTubeIndex = -1;

        for (int i = 0; i < currentState.getCurrentState().size(); i++) {

            Tube tube = currentState.getCurrentState().get(i);
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

    // Выполняем перенос капель
    private void makeTransfer() {
        // Берём пробирку k
        for (int k = 0; k < tubesAmount; k++) {
            //  и выполняем перенос
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

    // Перенос капли/капель
    private boolean executeTransfer(int departureTubeIndex) {
        // Получаем массив собранных капель
        List<Drop> dropsToTransfer = pickDropsToTransfer(departureTubeIndex);

        // Если массив не пуст, продолжаем
        if (!dropsToTransfer.isEmpty()) {
            // Вычисляем, куда можно поместить выбранные капли
            int destinationTubeIndex = pickTubeToDeliver(departureTubeIndex, dropsToTransfer);

            // Если нет известных тупиковых состояний, то продолжаем
            if (!blockingHistory.isEmpty()) {
                // Записываем потенциальный текущий хода
                int[] currentStep = new int[]{departureTubeIndex, destinationTubeIndex};
                int currentStepIndex = steps.size() - 1;
                // Если в истории тупиков есть данные для текущего шага (у этого хода были безысходные ситуации)
                if (!blockingHistory.get(currentStepIndex).isEmpty()) {
                    List<int[]> currentBlockedStatesList;
                    // Получаем список тупиковых вариантов
                    currentBlockedStatesList = blockingHistory.get(currentStepIndex);

                    // Смотрим, есть ли в списке тот, который собираемся сделать
                    for (int[] step: currentBlockedStatesList) {
                        // Если да, то прерываем перенос как неудавшийся
                        if (Arrays.equals(step, currentStep)) {
                            return false;
                        }
                    }
                }
            }

            // Если тупиковых состояний нет/есть, но текущий ход не был найден в списке тупиков
            // Проверяем, получилось ли найти пробирку, в которую возможен перенос
            if (destinationTubeIndex >= 0) {
                Tube tubeToPick = currentState.getCurrentState().get(departureTubeIndex);
                Tube tubeToFill = currentState.getCurrentState().get(destinationTubeIndex);
                List<Drop> modifiedContentsOfOriginalTube = tubeToPick.getContents();
                int x = Math.min(tubeToFill.getFreeCells(), dropsToTransfer.size());

                // Переносим капли
                for (int i = 0; i < x; i++) {
                    modifiedContentsOfOriginalTube.remove(modifiedContentsOfOriginalTube.getLast());
                    tubeToFill.getContents().add(dropsToTransfer.getLast());
                    dropsToTransfer.remove(dropsToTransfer.getLast());
                }

                tubeToPick.setContents(modifiedContentsOfOriginalTube);
                currentState.getCurrentState().set(departureTubeIndex, new Tube(tubeToPick));

                currentState = new State(currentState);
                printState(" " + departureTubeIndex + " >> " + destinationTubeIndex + " ");

                // Записываем ход как успешный
                steps.add(new int[]{departureTubeIndex, destinationTubeIndex});

                if (steps.size() != blockingHistory.size()) {
                    blockingHistory.add(new ArrayList<>());
                }
                return true;
            }
        }
        // Выходим с неудачей, если в пробирке не нашлось капель для переноса
        return false;
    }

    // Откат, на случай, если текущая цепочка ходов привела к тупику
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

    // Проверка, отсортированы ли все пробирки
    private boolean sortSucceeded() {
        List<Tube> sortedTubes = new ArrayList<>();
        int amountOfSortedTubes;
        for (int i = 0; i < tubesAmount; i++) {
            Tube tube = currentState.getCurrentState().get(i);

            if (!(tube.getContents().isEmpty())) {
                if (tube.isSorted()) {
                    sortedTubes.add(tube);
                }
            }
        }
        amountOfSortedTubes = sortedTubes.size();
        return amountOfSortedTubes == dropsVariety;
    }

    // Вывод итоговой последовательности ходов
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
