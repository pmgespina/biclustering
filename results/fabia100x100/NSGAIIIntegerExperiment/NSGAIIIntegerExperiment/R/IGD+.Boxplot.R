postscript("IGD+.Boxplot.eps", horizontal=FALSE, onefile=FALSE, height=8, width=12, pointsize=10)
resultDirectory<-"../data"
qIndicator <- function(indicator, problem)
{
fileNSGAIIa<-paste(resultDirectory, "NSGAIIa", sep="/")
fileNSGAIIa<-paste(fileNSGAIIa, problem, sep="/")
fileNSGAIIa<-paste(fileNSGAIIa, indicator, sep="/")
NSGAIIa<-scan(fileNSGAIIa)

fileNSGAIIb<-paste(resultDirectory, "NSGAIIb", sep="/")
fileNSGAIIb<-paste(fileNSGAIIb, problem, sep="/")
fileNSGAIIb<-paste(fileNSGAIIb, indicator, sep="/")
NSGAIIb<-scan(fileNSGAIIb)

fileNSGAIIc<-paste(resultDirectory, "NSGAIIc", sep="/")
fileNSGAIIc<-paste(fileNSGAIIc, problem, sep="/")
fileNSGAIIc<-paste(fileNSGAIIc, indicator, sep="/")
NSGAIIc<-scan(fileNSGAIIc)

fileNSGAIId<-paste(resultDirectory, "NSGAIId", sep="/")
fileNSGAIId<-paste(fileNSGAIId, problem, sep="/")
fileNSGAIId<-paste(fileNSGAIId, indicator, sep="/")
NSGAIId<-scan(fileNSGAIId)

algs<-c("NSGAIIa","NSGAIIb","NSGAIIc","NSGAIId")
boxplot(NSGAIIa,NSGAIIb,NSGAIIc,NSGAIId,names=algs, notch = FALSE)
titulo <-paste(indicator, problem, sep=":")
title(main=titulo)
}
par(mfrow=c(2,3))
indicator<-"IGD+"
qIndicator(indicator, "Multi Objective Integer Encoding Biclustering")
