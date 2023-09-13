export class Normalform {
  // public static readonly placeholder = new Normalform(
  //   '',
  //   'fDAssignment.normalform.placeholder'
  // );
  public static readonly BCNF = new Normalform(
    'http://www.dke.uni-linz.ac.at/etutorpp/Normalform#BCNF',
    'fDAssignment.normalform.bcnf'
  );
  public static readonly THIRD = new Normalform(
    'http://www.dke.uni-linz.ac.at/etutorpp/Normalform#THIRD',
    'fDAssignment.normalform.third'
  );
  public static readonly SECOND = new Normalform(
    'http://www.dke.uni-linz.ac.at/etutorpp/Normalform#SECOND',
    'fDAssignment.normalform.second'
  );
  public static readonly FIRST = new Normalform(
    'http://www.dke.uni-linz.ac.at/etutorpp/Normalform#FIRST',
    'fDAssignment.normalform.first'
  );
  public static readonly Values = [Normalform.BCNF, Normalform.THIRD, Normalform.SECOND, Normalform.FIRST];

  private readonly _value: string;
  private readonly _text: string;

  /**
   * Constructor.
   *
   * @param value the value
   * @param text the text
   */
  constructor(value: string, text: string) {
    this._value = value;
    this._text = text;
  }

  /**
   * Returns the value.
   */
  public get value(): string {
    return this._value;
  }

  /**
   * Returns the text.
   */
  public get text(): string {
    return this._text;
  }

  /**
   * Overridden toString method.
   */
  public toString = (): string => this._text;
}
