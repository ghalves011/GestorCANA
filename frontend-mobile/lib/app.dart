import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'core/theme/app_theme.dart';
import 'features/contributions/screens/contributions_screen.dart';
import 'features/home/screens/home_screen.dart';
import 'features/home/screens/matches_menu_screen.dart';
import 'features/matches/screens/attendance_selection_screen.dart';
import 'features/matches/screens/match_history_screen.dart';
import 'features/matches/screens/match_live_screen.dart';
import 'features/matches/screens/match_setup_screen.dart';
import 'features/players/screens/player_form_screen.dart';
import 'features/players/screens/player_search_screen.dart';
import 'features/settings/screens/settings_screen.dart';
import 'features/statistics/screens/statistics_screen.dart';

/// Named routes cover every screen from the desktop's menu structure
/// (plan §5). A few screens that always need rich non-serializable state
/// (a whole Partida, a selection result) are pushed via `extra` rather
/// than encoding that state into the URL, since they were never meant to
/// be deep-linkable — mirrors how the desktop opened them as modeless
/// child windows rather than top-level menu destinations.
final GoRouter appRouter = GoRouter(
  initialLocation: '/',
  routes: <RouteBase>[
    GoRoute(path: '/', builder: (BuildContext context, GoRouterState state) => const HomeScreen()),
    GoRoute(path: '/settings', builder: (BuildContext context, GoRouterState state) => const SettingsScreen()),
    GoRoute(path: '/partidas', builder: (BuildContext context, GoRouterState state) => const MatchesMenuScreen()),
    GoRoute(
      path: '/partidas/historico',
      builder: (BuildContext context, GoRouterState state) => const MatchHistoryScreen(),
    ),
    GoRoute(
      path: '/partidas/nova',
      builder: (BuildContext context, GoRouterState state) => const MatchSetupScreen(),
    ),
    GoRoute(
      path: '/partidas/live',
      builder: (BuildContext context, GoRouterState state) =>
          MatchLiveScreen(args: state.extra! as MatchLiveArgs),
    ),
    GoRoute(
      path: '/partidas/selecionar-jogadores',
      builder: (BuildContext context, GoRouterState state) =>
          AttendanceSelectionScreen(args: state.extra! as AttendanceSelectionArgs),
    ),
    GoRoute(
      path: '/contribuicoes',
      builder: (BuildContext context, GoRouterState state) => const ContributionsScreen(),
    ),
    GoRoute(
      path: '/estatisticas',
      builder: (BuildContext context, GoRouterState state) => const StatisticsScreen(),
    ),
    GoRoute(
      path: '/jogadores/novo',
      builder: (BuildContext context, GoRouterState state) => const PlayerFormScreen(),
    ),
    GoRoute(
      path: '/jogadores/:id/editar',
      builder: (BuildContext context, GoRouterState state) =>
          PlayerFormScreen(jogadorId: int.parse(state.pathParameters['id']!)),
    ),
    GoRoute(
      path: '/jogadores/buscar',
      builder: (BuildContext context, GoRouterState state) => const PlayerSearchScreen(),
    ),
  ],
);

class GestorCanaApp extends StatelessWidget {
  const GestorCanaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'GestorCANA',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(),
      routerConfig: appRouter,
    );
  }
}
