package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        ArmyBuilder builder = new ArmyBuilder(unitList, maxPoints);
        return builder.build();
    }

    private static class ArmyBuilder {
        private final List<Unit> availableTypes;
        private final int budget;
        private final GridAllocator gridAllocator;

        ArmyBuilder(List<Unit> types, int budget) {
            this.availableTypes = types;
            this.budget = budget;
            this.gridAllocator = new GridAllocator();
        }

        Army build() {
            UnitSelector selector = new UnitSelector(availableTypes, budget);
            PurchaseResult purchase = selector.selectUnits();

            List<Unit> instances = instantiateUnits(purchase);

            Army result = new Army(instances);
            result.setPoints(purchase.totalSpent);

            return result;
        }

        private List<Unit> instantiateUnits(PurchaseResult purchase) {
            List<Unit> units = new ArrayList<>();
            List<GridCell> cells = gridAllocator.allocate();

            int cellIdx = 0;

            int typeIdx = 0;
            while (typeIdx < availableTypes.size()) {
                Unit prototype = availableTypes.get(typeIdx);
                int quantity = purchase.quantities[typeIdx];

                int instanceNum = 1;
                while (instanceNum <= quantity && cellIdx < cells.size()) {
                    GridCell cell = cells.get(cellIdx);

                    Unit instance = new Unit(
                        prototype.getUnitType() + " " + instanceNum,
                        prototype.getUnitType(),
                        prototype.getHealth(),
                        prototype.getBaseAttack(),
                        prototype.getCost(),
                        prototype.getAttackType(),
                        prototype.getAttackBonuses(),
                        prototype.getDefenceBonuses(),
                        cell.col, cell.row
                    );

                    instance.setAlive(true);
                    instance.setProgram(prototype.getProgram());

                    units.add(instance);

                    instanceNum++;
                    cellIdx++;
                }

                typeIdx++;
            }

            return units;
        }
    }

    private static class UnitSelector {
        private final List<Unit> types;
        private final int budget;

        UnitSelector(List<Unit> types, int budget) {
            this.types = types;
            this.budget = budget;
        }

        PurchaseResult selectUnits() {
            ValueMetric[] metrics = computeMetrics();
            sortMetrics(metrics);

            int[] quantities = new int[types.size()];
            int spent = 0;

            int i = 0;
            while (i < metrics.length) {
                ValueMetric metric = metrics[i];
                Unit type = metric.unitType;
                int cost = type.getCost();

                int remaining = budget - spent;
                int maxAffordable = remaining / cost;
                int maxAllowed = 11;
                int toBuy = Math.min(maxAffordable, maxAllowed);

                if (toBuy > 0) {
                    int typeIndex = findTypeIndex(type);
                    quantities[typeIndex] = toBuy;
                    spent += toBuy * cost;
                }

                i++;
            }

            return new PurchaseResult(quantities, spent);
        }

        private ValueMetric[] computeMetrics() {
            ValueMetric[] metrics = new ValueMetric[types.size()];

            for (int i = 0; i < types.size(); i++) {
                Unit type = types.get(i);
                int cost = type.getCost();
                int atk = type.getBaseAttack();
                int hp = type.getHealth();

                double atkValue = (double) atk / cost;
                double hpValue = (double) hp / cost;
                double combinedValue = atkValue * 2.0 + hpValue;

                metrics[i] = new ValueMetric(type, combinedValue);
            }

            return metrics;
        }

        private void sortMetrics(ValueMetric[] metrics) {
            int n = metrics.length;
            for (int i = 1; i < n; i++) {
                ValueMetric key = metrics[i];
                int j = i - 1;

                while (j >= 0 && metrics[j].value < key.value) {
                    metrics[j + 1] = metrics[j];
                    j--;
                }

                metrics[j + 1] = key;
            }
        }

        private int findTypeIndex(Unit type) {
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i).getUnitType().equals(type.getUnitType())) {
                    return i;
                }
            }
            return 0;
        }
    }

    private static class ValueMetric {
        final Unit unitType;
        final double value;

        ValueMetric(Unit unitType, double value) {
            this.unitType = unitType;
            this.value = value;
        }
    }

    private static class PurchaseResult {
        final int[] quantities;
        final int totalSpent;

        PurchaseResult(int[] quantities, int totalSpent) {
            this.quantities = quantities;
            this.totalSpent = totalSpent;
        }
    }

    private static class GridAllocator {
        List<GridCell> allocate() {
            List<GridCell> grid = new ArrayList<>();

            int y = 0;
            while (y < 21) {
                int x = 0;
                while (x < 3) {
                    grid.add(new GridCell(x, y));
                    x++;
                }
                y++;
            }

            Collections.shuffle(grid);

            return grid;
        }
    }

    private static class GridCell {
        final int col;
        final int row;

        GridCell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
