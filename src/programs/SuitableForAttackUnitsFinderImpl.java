package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        FrontlineFinder finder = new FrontlineFinder(isLeftArmyTarget);
        return finder.extractFrontlineUnits(unitsByRow);
    }

    private static class FrontlineFinder {
        private final boolean seekingMinY;

        FrontlineFinder(boolean isLeftArmyTarget) {
            this.seekingMinY = isLeftArmyTarget;
        }

        List<Unit> extractFrontlineUnits(List<List<Unit>> allRows) {
            List<Unit> frontline = new ArrayList<>();

            int rowIndex = 0;
            while (rowIndex < allRows.size()) {
                List<Unit> currentRow = allRows.get(rowIndex);

                if (currentRow != null && !currentRow.isEmpty()) {
                    Unit frontlineUnit = selectFrontlineFromRow(currentRow);
                    if (frontlineUnit != null) {
                        frontline.add(frontlineUnit);
                    }
                }

                rowIndex++;
            }

            return frontline;
        }

        private Unit selectFrontlineFromRow(List<Unit> row) {
            Unit selected = null;
            int extremeY = seekingMinY ? Integer.MAX_VALUE : Integer.MIN_VALUE;

            int i = 0;
            while (i < row.size()) {
                Unit candidate = row.get(i);

                if (candidate != null && candidate.isAlive()) {
                    int candidateY = candidate.getyCoordinate();

                    if (isBetterCandidate(candidateY, extremeY)) {
                        selected = candidate;
                        extremeY = candidateY;
                    }
                }

                i++;
            }

            return selected;
        }

        private boolean isBetterCandidate(int candidateY, int currentExtremeY) {
            if (seekingMinY) {
                return candidateY < currentExtremeY;
            } else {
                return candidateY > currentExtremeY;
            }
        }
    }
}
