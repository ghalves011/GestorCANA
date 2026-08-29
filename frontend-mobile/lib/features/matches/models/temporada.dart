class Temporada {
  Temporada({this.id, required this.ano});

  int? id;
  int ano;

  factory Temporada.fromJson(Map<String, dynamic> json) {
    return Temporada(id: json['id'] as int?, ano: json['ano'] as int);
  }

  Map<String, dynamic> toJson() => <String, dynamic>{'id': id, 'ano': ano};

  @override
  String toString() => 'Temporada $ano';
}
