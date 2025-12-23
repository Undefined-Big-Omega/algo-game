package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.List;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    public void setPrintBattleLog(PrintBattleLog printBattleLog) {
        this.printBattleLog = printBattleLog;
    }

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        BattleContext context = new BattleContext(playerArmy, computerArmy);

        while (context.bothArmiesAlive()) {
            context.performBattleRound(printBattleLog);
        }
    }

    private static class BattleContext {
        private final Army playerArmy;
        private final Army computerArmy;

        BattleContext(Army playerArmy, Army computerArmy) {
            this.playerArmy = playerArmy;
            this.computerArmy = computerArmy;
        }

        boolean bothArmiesAlive() {
            return hasAliveUnits(playerArmy) && hasAliveUnits(computerArmy);
        }

        void performBattleRound(PrintBattleLog logger) throws InterruptedException {
            List<UnitWithPriority> prioritizedUnits = buildPriorityList();

            for (UnitWithPriority wrapper : prioritizedUnits) {
                if (!bothArmiesAlive()) {
                    return;
                }

                Unit unit = wrapper.unit;
                if (unit.isAlive() && unit.getProgram() != null) {
                    Unit target = unit.getProgram().attack();
                    if (logger != null) {
                        logger.printBattleLog(unit, target);
                    }
                }
            }
        }

        private List<UnitWithPriority> buildPriorityList() {
            List<UnitWithPriority> compUnits = new ArrayList<>();
            List<UnitWithPriority> playUnits = new ArrayList<>();

            if (computerArmy != null && computerArmy.getUnits() != null) {
                for (Unit u : computerArmy.getUnits()) {
                    if (u != null && u.isAlive()) {
                        compUnits.add(new UnitWithPriority(u));
                    }
                }
            }

            if (playerArmy != null && playerArmy.getUnits() != null) {
                for (Unit u : playerArmy.getUnits()) {
                    if (u != null && u.isAlive()) {
                        playUnits.add(new UnitWithPriority(u));
                    }
                }
            }

            List<UnitWithPriority> result = new ArrayList<>();
            int compIdx = 0;
            int playIdx = 0;

            while (compIdx < compUnits.size() || playIdx < playUnits.size()) {
                if (compIdx < compUnits.size()) {
                    result.add(compUnits.get(compIdx));
                    compIdx++;
                }
                if (playIdx < playUnits.size()) {
                    result.add(playUnits.get(playIdx));
                    playIdx++;
                }
            }

            sortByAttackDescending(result);

            return result;
        }

        private void sortByAttackDescending(List<UnitWithPriority> list) {
            mergeSort(list, 0, list.size() - 1);
        }

        private void mergeSort(List<UnitWithPriority> list, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;
                mergeSort(list, left, mid);
                mergeSort(list, mid + 1, right);
                merge(list, left, mid, right);
            }
        }

        private void merge(List<UnitWithPriority> list, int left, int mid, int right) {
            int n1 = mid - left + 1;
            int n2 = right - mid;

            List<UnitWithPriority> leftList = new ArrayList<>();
            List<UnitWithPriority> rightList = new ArrayList<>();

            for (int i = 0; i < n1; i++) {
                leftList.add(list.get(left + i));
            }
            for (int j = 0; j < n2; j++) {
                rightList.add(list.get(mid + 1 + j));
            }

            int i = 0, j = 0, k = left;

            while (i < n1 && j < n2) {
                if (leftList.get(i).attackPower >= rightList.get(j).attackPower) {
                    list.set(k, leftList.get(i));
                    i++;
                } else {
                    list.set(k, rightList.get(j));
                    j++;
                }
                k++;
            }

            while (i < n1) {
                list.set(k, leftList.get(i));
                i++;
                k++;
            }

            while (j < n2) {
                list.set(k, rightList.get(j));
                j++;
                k++;
            }
        }

        private boolean hasAliveUnits(Army army) {
            if (army == null || army.getUnits() == null) {
                return false;
            }

            for (Unit unit : army.getUnits()) {
                if (unit != null && unit.isAlive()) {
                    return true;
                }
            }

            return false;
        }
    }

    private static class UnitWithPriority {
        final Unit unit;
        final int attackPower;

        UnitWithPriority(Unit unit) {
            this.unit = unit;
            this.attackPower = unit.getBaseAttack();
        }
    }
}