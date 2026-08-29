/// Generic local substring filter, case-insensitive/accent-tolerant-free
/// (matches the backend's own case-insensitive comparisons elsewhere).
///
/// This exists specifically because POST /jogadores/filtrar-texto does NOT
/// exist on the backend (confirmed missing — see plan §6). The desktop
/// client already falls back to an equivalent local filter when that call
/// fails; here it's simply the only path, used for player search, the
/// substitution screen's bench search, and attendance-selection search.
List<T> filterBySubstring<T>(
  List<T> items,
  String query,
  List<String Function(T item)> fieldSelectors,
) {
  final String needle = query.trim().toLowerCase();
  if (needle.isEmpty) return items;

  return items.where((T item) {
    for (final String Function(T) selector in fieldSelectors) {
      if (selector(item).toLowerCase().contains(needle)) return true;
    }
    return false;
  }).toList();
}
