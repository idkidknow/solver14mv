interface NumberClue {
  type: "number";
  i: number;
  j: number;
  value: number;
}

interface QuestionMarkClue {
  type: "questionMark";
  i: number;
  j: number;
}

export type Clue = NumberClue | QuestionMarkClue;
