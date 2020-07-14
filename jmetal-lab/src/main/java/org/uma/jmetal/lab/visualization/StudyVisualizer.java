package org.uma.jmetal.lab.visualization;

import org.uma.jmetal.lab.visualization.html.*;
import org.uma.jmetal.lab.visualization.html.componentsImpl.*;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.BoxTrace;
import tech.tablesaw.plotly.traces.Scatter3DTrace;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.io.IOException;
import java.util.LinkedList;

public class StudyVisualizer {

    public static final String SHOW_BEST_FRONTS = "BEST";
    public static final String SHOW_MEDIAN_FRONTS = "MEDIAN";

    private static final String INDICATOR_SUMMARY_CSV = "QualityIndicatorSummary.csv";
    // NAMES OF CSV COLUMNS
    private static final String ALGORITHM = "Algorithm";
    private static final String PROBLEM = "Problem";
    private static final String INDICATOR_NAME = "IndicatorName";
    private static final String EXECUTION_ID = "ExecutionId";
    private static final String INDICATOR_VALUE = "IndicatorValue";

    private String folderPath;
    private Table table;
    private String showFronts;

    public static void main (String[] args) throws IOException {
        String directory = "StudyNSGAII3D/StudyNSGAII3D.4run";

        StudyVisualizer visualizer = new StudyVisualizer(directory, StudyVisualizer.SHOW_MEDIAN_FRONTS);

        visualizer.createHTMLPageForEachIndicator();
    }

    public StudyVisualizer(String path, String showFronts) throws IOException {
        folderPath = path;
        table = Table.read().csv(path + "/" + INDICATOR_SUMMARY_CSV);
        this.showFronts = showFronts;
    }

    public StudyVisualizer(String path) throws IOException {
        this(path, null);
    }

    public void createHTMLPageForEachIndicator() throws IOException {
        StringColumn indicators = getUniquesValuesOfStringColumn(INDICATOR_NAME);
        for (String indicator : indicators) {
            Html htmlPage = createHtmlPage(indicator);
            htmlPage.setPathFolder(folderPath + "/html");
            htmlPage.save();
        }
    }

    Html createHtmlPage(String indicator) throws IOException {
        Html htmlPage = new Html(indicator);

        HtmlGridView tablesGridView = createTestTables(indicator);
        htmlPage.addComponent(tablesGridView);

        StringColumn problems = getUniquesValuesOfStringColumn(PROBLEM);
        if (showFronts == null) {
            HtmlGridView boxPlotsGrid = new HtmlGridView();
            for (String problem : problems) {
                HtmlFigure figure = createBoxPlot(indicator, problem);
                boxPlotsGrid.addComponent(figure);
            }
            htmlPage.addComponent(boxPlotsGrid);
        } else {
            for (String problem : problems) {
                HtmlGridView gridView = createInformationForEachProblem(indicator, problem);
                htmlPage.addComponent(gridView);
            }
        }
        return htmlPage;
    }

    private HtmlGridView createTestTables(String indicator) {
        StringColumn algorithms = getUniquesValuesOfStringColumn(ALGORITHM);
        StringColumn problems = getUniquesValuesOfStringColumn(PROBLEM);
        Table tableFilteredByIndicator = filterTableByIndicator(table, indicator);

        boolean minimizar = true;

        HtmlTable medianValuesTable = new MedianValuesTable(tableFilteredByIndicator, indicator, algorithms, problems, INDICATOR_VALUE);
        HtmlTable wilconxonTable = new WilconxonTestTable(tableFilteredByIndicator, indicator, algorithms, problems, INDICATOR_VALUE);
        if (indicator.equals("HV")) {
            //tableFilteredByIndicator = convertTableToMinimize(tableFilteredByIndicator);
            minimizar = false;
        }
        HtmlTable friedmanTable = new FriedmanTestTable(tableFilteredByIndicator, algorithms, problems, minimizar);

        HtmlGridView htmlGridView = new HtmlGridView();
        htmlGridView.addComponent(medianValuesTable);
        htmlGridView.addComponent(wilconxonTable);
        htmlGridView.addComponent(friedmanTable);
        return htmlGridView;
    }

    private HtmlGridView createInformationForEachProblem(String indicator, String problem) throws IOException {
        HtmlGridView gridView = new HtmlGridView(problem);
        HtmlFigure boxPlot = createBoxPlot(indicator, problem);
        gridView.addComponent(boxPlot);
        StringColumn algorithms = getUniquesValuesOfStringColumn(ALGORITHM);
        for (String algorithm : algorithms) {
            HtmlFigure frontPlot = createFrontPlot(indicator, problem, algorithm);
            gridView.addComponent(frontPlot);
        }
        return gridView;
    }

    private HtmlFigure createBoxPlot(String indicator, String problem) {
        Table tableFilteredByIndicator = filterTableByIndicator(table, indicator);
        Table tableFilteredByIndicatorAndProblem =
                filterTableByProblem(tableFilteredByIndicator, problem);
        BoxTrace trace =
                BoxTrace.builder(
                        tableFilteredByIndicatorAndProblem.categoricalColumn(ALGORITHM),
                        tableFilteredByIndicatorAndProblem.doubleColumn(INDICATOR_VALUE))
                        .build();
        Layout layout = Layout.builder().title(problem).build();
        Figure figure = new Figure(layout, trace);
        return new HtmlFigure(figure);
    }

    private HtmlFigure createFrontPlot(String indicator, String problem, String algorithm) throws IOException {
        String csv = algorithm + "/" + problem + "/" + showFronts + "_" + indicator + "_FUN.csv";
        String path = folderPath + "/data/" + csv;
        CsvReadOptions csvReader = CsvReadOptions.builder(path).header(false).build();
        Table funTable = Table.read().usingOptions(csvReader);
        int numberOfObjectives = funTable.columnCount();
        Trace scatterTrace = null;
        if (numberOfObjectives == 2) {
            scatterTrace = ScatterTrace
                    .builder(funTable.column(0), funTable.column(1))
                    .build();
        } else if (numberOfObjectives == 3) {
            scatterTrace = Scatter3DTrace
                    .builder(funTable.numberColumn(0), funTable.numberColumn(1), funTable.numberColumn(2))
                    .build();
        }
        Layout layout = Layout.builder().title(algorithm).build();
        Figure figure = new Figure(layout, scatterTrace);
        return new HtmlFigure(figure);
    }

    private StringColumn getUniquesValuesOfStringColumn(String columnName) {
        return dropDuplicateRowsInColumn(table.stringColumn(columnName));
    }

    public StringColumn dropDuplicateRowsInColumn(StringColumn column) {
        LinkedList<String> result = new LinkedList<String>();
        for (int row = 0; row < column.size(); row++) {
            if (!result.contains(column.get(row))) {
                result.add(column.get(row));
            }
        }
        return StringColumn.create(column.name(), result);
    }

    private Table filterTableByIndicator(Table table, String indicator) {
        return table.where(table.stringColumn(INDICATOR_NAME).isEqualTo(indicator));
    }

    private Table filterTableByProblem(Table table, String problem) {
        return table.where(table.stringColumn(PROBLEM).isEqualTo(problem));
    }

    private Table filterTableByAlgorithm(Table table, String algorithm) {
        return table.where(table.stringColumn(ALGORITHM).isEqualTo(algorithm));
    }

    private Table convertTableToMinimize(Table table) {
        NumericColumn values = table.numberColumn(INDICATOR_VALUE);
        for (int i=0; i<table.rowCount(); i++) {
            values.set(i, values.getDouble(i) * -1);
        }
        return table;
    }
}