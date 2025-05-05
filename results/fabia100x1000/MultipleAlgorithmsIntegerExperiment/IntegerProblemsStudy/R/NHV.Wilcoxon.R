write("", "NHV.Wilcoxon.tex",append=FALSE)
resultDirectory<-"../data"
latexHeader <- function() {
  write("\\documentclass{article}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\title{StandardStudy}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\usepackage{amssymb}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\author{A.J.Nebro}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\begin{document}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\maketitle", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\section{Tables}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\", "NHV.Wilcoxon.tex", append=TRUE)
}

latexTableHeader <- function(problem, tabularString, latexTableFirstLine) {
  write("\\begin{table}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\caption{", "NHV.Wilcoxon.tex", append=TRUE)
  write(problem, "NHV.Wilcoxon.tex", append=TRUE)
  write(".NHV.}", "NHV.Wilcoxon.tex", append=TRUE)

  write("\\label{Table:", "NHV.Wilcoxon.tex", append=TRUE)
  write(problem, "NHV.Wilcoxon.tex", append=TRUE)
  write(".NHV.}", "NHV.Wilcoxon.tex", append=TRUE)

  write("\\centering", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\setlength\\tabcolsep{1pt}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\begin{scriptsize}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\begin{tabular}{", "NHV.Wilcoxon.tex", append=TRUE)
  write(tabularString, "NHV.Wilcoxon.tex", append=TRUE)
  write("}", "NHV.Wilcoxon.tex", append=TRUE)
  write(latexTableFirstLine, "NHV.Wilcoxon.tex", append=TRUE)
  write("\\hline ", "NHV.Wilcoxon.tex", append=TRUE)
}

printTableLine <- function(indicator, algorithm1, algorithm2, i, j, problem) { 
  file1<-paste(resultDirectory, algorithm1, sep="/")
  file1<-paste(file1, problem, sep="/")
  file1<-paste(file1, indicator, sep="/")
  data1<-scan(file1)
  file2<-paste(resultDirectory, algorithm2, sep="/")
  file2<-paste(file2, problem, sep="/")
  file2<-paste(file2, indicator, sep="/")
  data2<-scan(file2)
  if (i == j) {
    write("-- ", "NHV.Wilcoxon.tex", append=TRUE)
  }
  else if (i < j) {
    if (is.finite(wilcox.test(data1, data2)$p.value) & wilcox.test(data1, data2)$p.value <= 0.05) {
      if (median(data1) <= median(data2)) {
        write("$\\blacktriangle$", "NHV.Wilcoxon.tex", append=TRUE)
}
      else {
        write("$\\triangledown$", "NHV.Wilcoxon.tex", append=TRUE)
}
    }
    else {
      write("--", "NHV.Wilcoxon.tex", append=TRUE)
    }
  }
  else {
    write(" ", "NHV.Wilcoxon.tex", append=TRUE)
  }
}

latexTableTail <- function() { 
  write("\\hline", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\end{tabular}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\end{scriptsize}", "NHV.Wilcoxon.tex", append=TRUE)
  write("\\end{table}", "NHV.Wilcoxon.tex", append=TRUE)
}

latexTail <- function() { 
  write("\\end{document}", "NHV.Wilcoxon.tex", append=TRUE)
}

### START OF SCRIPT 
# Constants
problemList <-c("Multi Objective Integer Encoding Biclustering") 
algorithmList <-c("NSGAII", "SPEA2", "MOCell") 
tabularString <-c("lcc") 
latexTableFirstLine <-c("\\hline  & SPEA2 & MOCell\\\\ ") 
indicator<-"NHV"

 # Step 1.  Writes the latex header
latexHeader()
tabularString <-c("| l | c | c | ") 

latexTableFirstLine <-c("\\hline \\multicolumn{1}{|c|}{} & \\multicolumn{1}{c|}{SPEA2} & \\multicolumn{1}{c|}{MOCell} \\\\") 

# Step 3. Problem loop 
latexTableHeader("Multi Objective Integer Encoding Biclustering ", tabularString, latexTableFirstLine)

indx = 0
for (i in algorithmList) {
  if (i != "MOCell") {
    write(i , "NHV.Wilcoxon.tex", append=TRUE)
    write(" & ", "NHV.Wilcoxon.tex", append=TRUE)

    jndx = 0
    for (j in algorithmList) {
      for (problem in problemList) {
        if (jndx != 0) {
          if (i != j) {
            printTableLine(indicator, i, j, indx, jndx, problem)
          }
          else {
            write("  ", "NHV.Wilcoxon.tex", append=TRUE)
          } 
          if (problem == "Multi Objective Integer Encoding Biclustering") {
            if (j == "MOCell") {
              write(" \\\\ ", "NHV.Wilcoxon.tex", append=TRUE)
            } 
            else {
              write(" & ", "NHV.Wilcoxon.tex", append=TRUE)
            }
          }
     else {
    write("&", "NHV.Wilcoxon.tex", append=TRUE)
     }
        }
      }
      jndx = jndx + 1
}
    indx = indx + 1
  }
} # for algorithm

  latexTableTail()

#Step 3. Writes the end of latex file 
latexTail()

