import sys

resultFile = open("result.txt", "rt")

with open("analysis.txt", "wt") as analysisFile:
	keyLen = len(sys.argv)
	if keyLen == 1:
		keyword = "TeamWatermelon"
	else:
		keyword = str("Team") + str(sys.argv[1])
	
	tmp = []
	mine = []
	oppo = []
	cnt = 0
	hit = False
	teamNameList = []
	scoreList = []

	for line in resultFile:
		tmp = line.split(' ')
		if tmp[0] == "Average":
			teamName = tmp[4][:-1]
			score = tmp[5][:-1]
			teamNameList.append(teamName)
			scoreList.append(score)
			cnt += 1
			# print(teamName + " " + score)

			# first case
			if teamName == keyword and cnt %2 != 0:
				mine.append(teamName + " " + score + " ")
				hit = True
			elif hit and cnt %2 == 0:
				oppo.append(teamName + " " + score + " ")
				hit = False
				cnt = 0 # double check this one

			# second case: when keywords occur later
			# will require previous storage
			elif teamName == keyword and cnt %2 == 0:
				mine.append(teamName + " " + score + " ")
				oppo.append(teamNameList[-2] + " " + scoreList[-2])

fmt = '%-4s%-25s%s'

print(fmt % ('', 'Mine', 'Oppo'))
for i, (m, o) in enumerate(zip(mine, oppo)):
    print(fmt % (i, m, o))

# print("overall name: "+ str(teamNameList))
# print()
# print("overall score: "+ str(scoreList))
# print()
# print("mine: " + str(mine))
# print()
# print("oppo: " + str(oppo))
analysisFile.close()
resultFile.close()
