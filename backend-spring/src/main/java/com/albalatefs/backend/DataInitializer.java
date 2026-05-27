package com.albalatefs.backend;

import com.albalatefs.backend.model.*;
import com.albalatefs.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final JugadorRepository jugadorRepo;
    private final PartidoRepository partidoRepo;
    private final NoticiaRepository noticiaRepo;
    private final SocioRepository socioRepo;
    private final EstadisticaJugadorRepository estadisticaRepo;
    private final VideoTacticoRepository videoRepo;
    private final ProductoRepository productoRepo;
    private final FavoritoRepository favoritoRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepo,
                           JugadorRepository jugadorRepo,
                           PartidoRepository partidoRepo,
                           NoticiaRepository noticiaRepo,
                           SocioRepository socioRepo,
                           EstadisticaJugadorRepository estadisticaRepo,
                           VideoTacticoRepository videoRepo,
                           ProductoRepository productoRepo,
                           FavoritoRepository favoritoRepo,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.jugadorRepo = jugadorRepo;
        this.partidoRepo = partidoRepo;
        this.noticiaRepo = noticiaRepo;
        this.socioRepo = socioRepo;
        this.estadisticaRepo = estadisticaRepo;
        this.videoRepo = videoRepo;
        this.productoRepo = productoRepo;
        this.favoritoRepo = favoritoRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Re-seed si la base de datos está vacía o si los datos no tienen los campos nuevos
        if (usuarioRepo.count() > 0) {
            boolean tieneNuevosCampos = jugadorRepo.count() > 0 &&
                jugadorRepo.findAll().get(0).getPiernasDominante() != null;
            boolean tieneRolEntrenador = usuarioRepo.findByEmail("edu@albalatefs.com")
                .map(u -> "ENTRENADOR".equals(u.getRol()))
                .orElse(false);
            // Verificar que las contraseñas están codificadas con BCrypt (empiezan por $2)
            // Si se migraron como texto plano el login fallará con "Bad credentials"
            boolean tienePasswordBcrypt = usuarioRepo.findByEmail("edu@albalatefs.com")
                .map(u -> u.getPassword() != null && u.getPassword().startsWith("$2"))
                .orElse(false);

            if (tieneNuevosCampos && tieneRolEntrenador && tienePasswordBcrypt) {
                // Usuarios/jugadores OK → solo seedear productos si faltan
                if (productoRepo.count() == 0) {
                    seedProductos();
                }
                // Crear usuarios SOCIO para los socios seeded que no tengan usuario aún
                Object[][] sociosData = {
                    {"miguel.lorente@gmail.com", "12345678A"},
                    {"rblasco@hotmail.com",       "23456789B"},
                    {"fernando.querol@gmail.com", "34567890C"},
                    {"lucia.moya@gmail.com",      "45678901D"},
                    {"jarino@outlook.com",        "56789012E"},
                    {"elena.pallares@gmail.com",  "67890123F"},
                    {"tguillen@gmail.com",        "78901234G"},
                    {"amparo.sancho@hotmail.com", "89012345H"},
                };
                for (Object[] d : sociosData) {
                    String email = (String) d[0];
                    String dni   = (String) d[1];
                    if (!usuarioRepo.existsByEmail(email)) {
                        usuarioRepo.save(new Usuario(null, email, passwordEncoder.encode(dni), "SOCIO"));
                        System.out.println("[INIT] Usuario SOCIO creado para: " + email);
                    }
                }
                return;
            }
            // Limpiar datos antiguos para re-sembrar (orden respetando FK constraints)
            estadisticaRepo.deleteAll();
            favoritoRepo.deleteAll();   // FK → usuarios, productos
            videoRepo.deleteAll();
            socioRepo.deleteAll();
            noticiaRepo.deleteAll();
            productoRepo.deleteAll();   // FK ← favoritos (ya borrado)
            jugadorRepo.deleteAll();    // FK → usuarios
            partidoRepo.deleteAll();
            usuarioRepo.deleteAll();
        }

        // ── USUARIOS ────────────────────────────────────────────────────────
        String pass = passwordEncoder.encode("albalate2026");

        Usuario uAitor   = usuarioRepo.save(new Usuario(null, "aitor@albalatefs.com",   pass, "JUGADOR"));
        Usuario uAnas    = usuarioRepo.save(new Usuario(null, "anas@albalatefs.com",     pass, "JUGADOR"));
        Usuario uBombo   = usuarioRepo.save(new Usuario(null, "bombo@albalatefs.com",    pass, "JUGADOR"));
        Usuario uCarlos  = usuarioRepo.save(new Usuario(null, "carlos@albalatefs.com",   pass, "JUGADOR"));
        Usuario uCesar   = usuarioRepo.save(new Usuario(null, "cesar@albalatefs.com",    pass, "JUGADOR"));
        Usuario uEdu     = usuarioRepo.save(new Usuario(null, "edu@albalatefs.com",      pass, "ENTRENADOR"));
        Usuario uFofi    = usuarioRepo.save(new Usuario(null, "fofi@albalatefs.com",     pass, "JUGADOR"));
        Usuario uGuille  = usuarioRepo.save(new Usuario(null, "guille@albalatefs.com",   pass, "JUGADOR"));
        Usuario uGuiral  = usuarioRepo.save(new Usuario(null, "guiral@albalatefs.com",   pass, "JUGADOR"));
        Usuario uManu    = usuarioRepo.save(new Usuario(null, "manu@albalatefs.com",     pass, "JUGADOR"));
        Usuario uRodrigo = usuarioRepo.save(new Usuario(null, "rodrigo@albalatefs.com",  pass, "JUGADOR"));
        Usuario uSergio  = usuarioRepo.save(new Usuario(null, "sergio@albalatefs.com",   pass, "JUGADOR"));
        Usuario uTono    = usuarioRepo.save(new Usuario(null, "tono@albalatefs.com",     pass, "JUGADOR"));
        usuarioRepo.save(new Usuario(null, "admin@albalatefs.com", passwordEncoder.encode("admin2026"), "ADMIN"));

        // ── JUGADORES ───────────────────────────────────────────────────────
        Jugador aitor   = jugadorRepo.save(new Jugador(null, "Aitor",     "Lazaro",           1,  "Portero",      "assets/jugadores/aitor.JPG",   LocalDate.of(1999,  3, 15), 2, "Derecha",    uAitor));
        Jugador anas    = jugadorRepo.save(new Jugador(null, "Anas",      "Benali Moussaoui", 2,  "Ala",          "assets/jugadores/anas.JPG",    LocalDate.of(2001,  7, 22), 2, "Ambas",    uAnas));
        Jugador bombo   = jugadorRepo.save(new Jugador(null, "Javier",    "Bernad",           15, "Pívot",        "assets/jugadores/bombo.JPG",   LocalDate.of(1997, 11,  8), 1, "Izquierda",    uBombo));
        Jugador carlos  = jugadorRepo.save(new Jugador(null, "Carlos",    "Bernad",           11, "Pívot",        "assets/jugadores/carlos.JPG",  LocalDate.of(1998,  5, 14), 2, "Derecha",    uCarlos));
        Jugador cesar   = jugadorRepo.save(new Jugador(null, "César",     "Gascón",           9,  "Pívot",        "assets/jugadores/cesar.JPG",   LocalDate.of(1995,  9, 30), 2, "Derecha",    uCesar));
        Jugador edu     = jugadorRepo.save(new Jugador(null, "Edu",       "Gascón",           9,  "Entrenador",   "assets/jugadores/edu.JPG",     LocalDate.of(1993,  4, 12), 2, "Derecha",    uEdu));
        Jugador fofi    = jugadorRepo.save(new Jugador(null, "Carlos",    "Garralaga",        13, "Portero",      "assets/jugadores/fofi.JPG",    LocalDate.of(2000,  2, 28), 2, "Derecha",    uFofi));
        Jugador guille  = jugadorRepo.save(new Jugador(null, "Guillermo", "Ayuda",            4,  "Cierre/Ala",   "assets/jugadores/guille.JPG",  LocalDate.of(1996,  8,  5), 1, "Derecha",  uGuille));
        Jugador guiral  = jugadorRepo.save(new Jugador(null, "Sergio",    "Guiral",           8,  "Cierre/Pívot", "assets/jugadores/guiral.JPG",  LocalDate.of(1998, 12, 19), 2, "Derecha",    uGuiral));
        Jugador manu    = jugadorRepo.save(new Jugador(null, "Manuel",    "Fernandez",        10, "Cierre",       "assets/jugadores/manu.JPG",    LocalDate.of(2002,  6, 11), 2, "Derecha",    uManu));
        Jugador rodrigo = jugadorRepo.save(new Jugador(null, "Rodrigo",   "Fernandez",        7,  "Pívot",        "assets/jugadores/rodrigo.JPG", LocalDate.of(1999, 10, 24), 2, "Izquierda",  uRodrigo));
        Jugador sergio  = jugadorRepo.save(new Jugador(null, "Sergio",    "Sauras",           6,  "Ala",          "assets/jugadores/sergio.JPG",  LocalDate.of(2000,  3,  7), 2, "Derecha",    uSergio));
        Jugador tono    = jugadorRepo.save(new Jugador(null, "Toño",      "Izquierdo",        5,  "Ala/Pívot",    "assets/jugadores/tono.JPG",    LocalDate.of(1994,  1, 18), 2, "Derecha",      uTono));

        // ── PARTIDOS ────────────────────────────────────────────────────────
        Partido p1  = partidoRepo.save(new Partido(null, ldt(2026, 1, 10, 19, 0), "Albalate FS", "CD Andorra",          5, 2, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p2  = partidoRepo.save(new Partido(null, ldt(2026, 1, 17, 18, 30), "FS Caspe",    "Albalate FS",         1, 4, "Pabellón FS Caspe",           "FINALIZADO"));
        Partido p3  = partidoRepo.save(new Partido(null, ldt(2026, 1, 24, 19, 0), "Albalate FS", "Azuara Futsal",       3, 3, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p4  = partidoRepo.save(new Partido(null, ldt(2026, 2, 7,  19, 0), "Albalate FS", "CD Belchite",         6, 1, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p5  = partidoRepo.save(new Partido(null, ldt(2026, 2, 14, 18, 0), "Fuendetodos FS", "Albalate FS",      0, 5, "Pabellón Fuendetodos",        "FINALIZADO"));
        Partido p6  = partidoRepo.save(new Partido(null, ldt(2026, 2, 21, 19, 0), "Albalate FS", "Híjar Futsal",        2, 1, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p7  = partidoRepo.save(new Partido(null, ldt(2026, 3, 7,  19, 0), "Albalate FS", "Quinto Futsal",       4, 0, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p8  = partidoRepo.save(new Partido(null, ldt(2026, 3, 14, 18, 30), "FS Alcañiz",  "Albalate FS",         3, 2, "Pabellón FS Alcañiz",         "FINALIZADO"));
        Partido p9  = partidoRepo.save(new Partido(null, ldt(2026, 3, 21, 19, 0), "Albalate FS", "CD La Puebla",        5, 1, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p10 = partidoRepo.save(new Partido(null, ldt(2026, 4, 4,  19, 0), "Albalate FS", "Azuara Futsal",       3, 2, "Pabellón Municipal Albalate", "FINALIZADO"));
        Partido p11 = partidoRepo.save(new Partido(null, ldt(2026, 4, 18, 18, 30), "CD Andorra",  "Albalate FS",         1, 3, "Pabellón CD Andorra",         "FINALIZADO"));
        Partido p12 = partidoRepo.save(new Partido(null, ldt(2026, 4, 25, 19, 0), "Albalate FS", "Híjar Futsal",        4, 2, "Pabellón Municipal Albalate", "FINALIZADO"));
        // Próximos partidos
        partidoRepo.save(new Partido(null, ldt(2026, 5, 16, 19, 0), "Albalate FS", "Quinto Futsal",  null, null, "Pabellón Municipal Albalate", "PROXIMO"));
        partidoRepo.save(new Partido(null, ldt(2026, 5, 23, 18, 30), "FS Caspe",    "Albalate FS",    null, null, "Pabellón FS Caspe",           "PROXIMO"));
        partidoRepo.save(new Partido(null, ldt(2026, 6, 6,  19, 0), "Albalate FS", "CD Belchite",    null, null, "Pabellón Municipal Albalate", "PROXIMO"));

        // ── NOTICIAS ────────────────────────────────────────────────────────
        noticiaRepo.save(new Noticia(null,
            "Primera toma de contacto de Albalate con el fútbol sala antes de la presentación oficial",
            "El próximo 14 de septiembre, en las jornadas por la inclusión se jugará un partido a modo de puesta de largo oficial del equipo. Este sábado, jugadores y directiva caldearon el ambiente en las prefiestas",
            "Para caldear el ambiente y hacer una primera toma de contacto con la afición, el sábado se planteó un partido entre jugadores y directiva que acabó por decantarse del lado de los segundos por 2 goles a 3.",
            ldt(2026, 1, 11, 10, 0), "Crónica", "assets/noticias/albalate-pretemporada-albalate-futbol-sala-directiva.jpg"));

        noticiaRepo.save(new Noticia(null,
            "Albalate saca pecho de su cantera en fútbol sala femenino y disputa la fase final del campeonato de Aragón",
            "El equipo infantil femenino, entrenado por Dani Bernad, terminó cuarto en el autonómico",
            "El trabajo de la cantera del fútbol sala femenino en Albalate del Arzobispo ya recoge sus frutos. El equipo infantil femenino, entrenado por el local Dani Bernad, logró el pasado fin de semana conquistar el cuarto puesto en el Campeonato de Aragón al que accedió el equipo como campeón provincial.",
            ldt(2026, 1, 18, 9, 30), "Crónica", "assets/noticias/download.jpg"));

        noticiaRepo.save(new Noticia(null,
            "El Albalate FS renueva a su portero Aitor Lazaro por dos temporadas",
            "El guardameta amplía su vinculación con el club hasta 2028.",
            "Aitor García, uno de los pilares fundamentales del equipo durante las últimas campañas, ha renovado su contrato con el Albalate FS por dos temporadas más. El portero, de 27 años, ha sido clave en la solidez defensiva del equipo con una media de menos de dos goles encajados por partido. 'Estoy muy feliz aquí, es mi casa', declaró tras firmar.",
            ldt(2026, 1, 25, 11, 0), "Club", "assets/noticias/renovacion-aitor.jpg"));

        noticiaRepo.save(new Noticia(null,
            "Nos despedimos de nuestros jugadores!",
            "Anunciamos las 3 primeras bajas del equipo.",
            "Tras una temporada con nosostros nos despedimos de Sergio, Daniel y Mario, un placer compartir la pista con vosotros y os deseamos lo mejor en el futuro.",
            ldt(2026, 2, 8, 10, 0), "Crónica", "assets/noticias/despedida.jpg"));

        noticiaRepo.save(new Noticia(null,
            "Albalate se une por la inclusión social y presenta su equipo de fútbol sala junto a ATADI",
            "Los jugadores albalatinos dan a conocer su cantera en un partido con miembros de ATADI de Andorra, Utrillas y Alcañiz. Su primer encuentro en liga será el viernes 27",
            "Por la inclusión y demostrar \"que todos somos iguales\". Esos fueron los motivos que llevaron a Albalate del Arzobispo a realizar la presentación oficial de su equipo de fútbol sala con un partido con miembros de los centros de ATADI de Andorra, Utrillas y Alcañiz. La iniciativa, que contó con éxito de participación, fue impulsada por primera vez el pasado verano y este año se ha querido repetir por todo lo que esta puede enseñar a la población.",
            ldt(2026, 3, 5, 12, 0), "Club", "assets/noticias/inclusion-social.jpg"));

        noticiaRepo.save(new Noticia(null,
            "«Joga Bonito» se impone en las 48 horas de fútbol sala de Albalate",
            "El torneo fue un éxito de asistencia y se vivió un gran ambiente en el pabellón durante todo el fin de semana",
            "as 48 horas de fútbol sala de Albalate del Arzobispo llegaron a su fin este domingo por la tarde. El equipo campeón fue Joga Bonito, procedente de Barcelona, que se impuso en la final por 1-3 al Pitux Team de Híjar. Cabe destacar que los campeones cuentan en su haber con el Mejor Jugador del Torneo, Carlos Gómez, y con el Pichichi, Sergio Costa.",
            ldt(2026, 3, 22, 9, 0), "Afición", "assets/noticias/horas.jpg"));

        noticiaRepo.save(new Noticia(null,
            "Nueva edicion del torneo local!",
            "El próximo sábado se decide quién se lleva el titulo local.",
            "El partido del sábado 16 de mayo enfrenta a los dos equipos con más opciones de obtener el titulo local la peña El Vicio contra La Colapso",
            ldt(2026, 5, 13, 8, 0), "Previa", "assets/noticias/torneo.jpg"));

        // ── SOCIOS ──────────────────────────────────────────────────────────
        // Datos: [email, dni]
        Object[][] sociosData = {
            {"miguel.lorente@gmail.com",  "12345678A", "Miguel",   "Lorente García",   "634111222", date(2024, 9, 1)},
            {"rblasco@hotmail.com",        "23456789B", "Raquel",   "Blasco Ferrer",    "645222333", date(2024, 9, 15)},
            {"fernando.querol@gmail.com",  "34567890C", "Fernando", "Querol Navarro",   "656333444", date(2024, 10, 3)},
            {"lucia.moya@gmail.com",       "45678901D", "Lucía",    "Moya Pérez",       "667444555", date(2024, 10, 20)},
            {"jarino@outlook.com",         "56789012E", "Javier",   "Ariño Estrada",    "678555666", date(2024, 11, 5)},
            {"elena.pallares@gmail.com",   "67890123F", "Elena",    "Pallarés Ibáñez",  "689666777", date(2025, 1, 10)},
            {"tguillen@gmail.com",         "78901234G", "Tomás",    "Guillén Morte",    "690777888", date(2025, 2, 14)},
            {"amparo.sancho@hotmail.com",  "89012345H", "Amparo",   "Sancho Villalba",  "601888999", date(2025, 3, 1)},
        };
        for (Object[] d : sociosData) {
            String email = (String) d[0];
            String dni   = (String) d[1];
            socioRepo.save(new Socio(null, (String) d[2], (String) d[3], dni, email, (String) d[4], (Date) d[5]));
            if (!usuarioRepo.existsByEmail(email)) {
                usuarioRepo.save(new Usuario(null, email, passwordEncoder.encode(dni), "SOCIO"));
            }
        }

        // ── ESTADÍSTICAS ────────────────────────────────────────────────────
        // Partidos finalizados: p1..p12
        List<Partido> finalizados = List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12);

        // [goles, asistencias, minutos, calificacion] por jugador y partido
        int[][][] stats = {
            // p1 (5-2 vs Andorra)
            {{2,1,40,8},{1,2,38,9},{0,0,20,6},{2,1,40,9},{0,1,40,8},{0,0,0,0},{1,0,35,7},{0,1,40,8},{0,0,40,7},{0,0,0,0},{0,1,30,7},{0,0,25,7},{0,0,20,6}},
            // p2 (1-4 en Caspe)
            {{0,0,40,7},{1,1,40,9},{0,1,35,8},{1,2,40,9},{0,0,40,8},{0,0,0,0},{0,0,30,7},{1,0,40,8},{0,1,40,8},{0,0,0,0},{1,1,35,8},{0,0,20,6},{0,0,15,6}},
            // p3 (3-3 vs Azuara)
            {{0,0,40,6},{0,1,40,7},{1,0,40,7},{1,1,40,8},{0,0,40,7},{1,0,30,7},{0,0,25,6},{0,1,35,7},{0,0,40,7},{0,0,0,0},{1,0,30,7},{0,0,20,6},{0,0,20,6}},
            // p4 (6-1 vs Belchite)
            {{0,0,40,8},{1,2,40,9},{0,1,35,8},{2,1,40,9},{0,0,40,8},{3,0,35,9},{0,0,0,0},{1,1,40,9},{0,0,40,7},{0,0,0,0},{0,1,30,7},{0,0,25,7},{0,0,20,7}},
            // p5 (0-5 en Fuendetodos)
            {{0,0,40,9},{2,1,38,9},{0,1,35,8},{1,2,40,8},{0,0,40,7},{0,0,30,7},{0,0,0,0},{1,0,40,8},{0,1,40,8},{0,0,0,0},{1,1,35,8},{0,0,25,7},{0,0,20,6}},
            // p6 (2-1 vs Híjar)
            {{0,0,40,8},{1,0,40,8},{0,1,35,7},{0,1,40,8},{0,0,40,7},{0,0,0,0},{0,0,30,7},{1,0,35,8},{0,0,40,7},{0,0,0,0},{0,1,30,7},{0,0,25,6},{0,0,20,6}},
            // p7 (4-0 vs Quinto)
            {{0,0,40,8},{1,1,40,9},{0,1,35,8},{1,1,40,8},{0,0,40,7},{1,0,35,8},{0,0,0,0},{0,1,40,7},{0,0,40,7},{0,0,0,0},{2,0,35,9},{0,0,25,7},{0,0,20,6}},
            // p8 (3-2 en Alcañiz)
            {{0,0,40,7},{0,1,40,8},{1,0,35,8},{1,1,40,8},{0,0,40,7},{0,0,0,0},{0,0,30,7},{1,1,40,8},{0,0,40,7},{0,0,0,0},{0,1,30,7},{0,0,25,6},{0,0,20,6}},
            // p9 (5-1 vs La Puebla)
            {{0,0,40,8},{2,0,40,9},{0,1,35,8},{1,2,40,9},{0,0,40,7},{1,0,30,8},{0,0,0,0},{0,1,40,8},{0,0,40,7},{0,0,0,0},{1,1,35,8},{0,0,25,7},{0,0,20,6}},
            // p10 (3-2 vs Azuara)
            {{0,0,40,8},{1,1,38,8},{0,1,35,8},{1,0,40,8},{0,0,40,7},{0,0,0,0},{0,0,30,7},{1,1,35,8},{0,0,40,7},{0,0,0,0},{0,1,30,7},{0,0,25,7},{0,0,20,6}},
            // p11 (3-1 en Andorra)
            {{0,0,40,8},{1,0,40,9},{0,1,35,8},{0,2,40,8},{0,0,40,7},{1,0,30,8},{0,0,0,0},{1,1,40,9},{0,0,40,7},{0,0,0,0},{0,1,35,7},{0,0,25,7},{0,0,20,6}},
            // p12 (4-2 vs Híjar)
            {{0,0,40,8},{1,1,40,9},{0,0,35,7},{2,1,40,9},{0,0,40,7},{1,0,30,8},{0,0,0,0},{0,1,40,8},{0,0,40,7},{0,0,0,0},{0,2,35,8},{0,0,25,7},{0,0,20,6}},
        };

        Jugador[] jugadores = {aitor, anas, bombo, carlos, cesar, edu, fofi, guille, guiral, manu, rodrigo, sergio, tono};
        for (int i = 0; i < finalizados.size(); i++) {
            for (int j = 0; j < jugadores.length; j++) {
                int[] s = stats[i][j];
                if (s[2] > 0) { // solo si jugó minutos
                    estadisticaRepo.save(new EstadisticaJugador(null,
                        jugadores[j], finalizados.get(i), s[0], s[1], s[2], s[3]));
                }
            }
        }

        // ── VÍDEOS TÁCTICOS ─────────────────────────────────────────────────
        videoRepo.save(new VideoTactico(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "Análisis defensivo jornada 1 - Bloque medio bajo", ldt(2026, 1, 12, 10, 0), List.of()));
        videoRepo.save(new VideoTactico(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "Corrección ofensiva: salida de balón desde portería", ldt(2026, 1, 19, 10, 0), List.of(aitor)));
        videoRepo.save(new VideoTactico(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "Movimientos en ataque posicional - Temporada 25/26", ldt(2026, 2, 10, 10, 0), List.of()));
        videoRepo.save(new VideoTactico(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "Análisis individual: finalización de Carlos - Jornada 4", ldt(2026, 2, 9, 11, 0), List.of(carlos)));
        videoRepo.save(new VideoTactico(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "Presión alta y recuperación de balón", ldt(2026, 3, 8, 10, 0), List.of()));

        // ── PRODUCTOS TIENDA ─────────────────────────────────────────────────
        if (productoRepo.count() == 0) {
            seedProductos();
        }

        System.out.println("✅ Base de datos inicializada con datos de prueba.");
    }

    private void seedProductos() {
        productoRepo.save(new Producto(null, "Camiseta Oficial 1ª Equipación 25/26",
            "Camiseta oficial de juego del Albalate FS. Tejido técnico transpirable. Tallas S-XXL.",
            new java.math.BigDecimal("35.00"),
            "https://images.unsplash.com/photo-1562575214-da9fcf59b907?w=400",
            "Ropa", 50, true));
        productoRepo.save(new Producto(null, "Camiseta Oficial 2ª Equipación 25/26",
            "Camiseta alternativa del Albalate FS. Edición limitada temporada 25/26.",
            new java.math.BigDecimal("35.00"),
            "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400",
            "Ropa", 30, false));
        productoRepo.save(new Producto(null, "Chándal Oficial Albalate FS",
            "Conjunto de chándal con escudo bordado. Ideal para calentamiento y uso casual.",
            new java.math.BigDecimal("55.00"),
            "https://images.unsplash.com/photo-1516478177764-9fe5bd7e9717?w=400",
            "Ropa", 20, true));
        productoRepo.save(new Producto(null, "Bufanda Albalate FS",
            "Bufanda de punto con los colores del club. Perfecta para animar en el pabellón.",
            new java.math.BigDecimal("12.00"),
            "https://images.unsplash.com/photo-1520903920243-00d872a2d1c9?w=400",
            "Accesorios", 100, false));
        productoRepo.save(new Producto(null, "Gorra Albalate FS",
            "Gorra ajustable con escudo bordado. Talla única.",
            new java.math.BigDecimal("15.00"),
            "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400",
            "Accesorios", 60, false));
        productoRepo.save(new Producto(null, "Llavero Escudo",
            "Llavero metálico con el escudo del Albalate FS. Acabado premium.",
            new java.math.BigDecimal("5.00"),
            "https://images.unsplash.com/photo-1558171813-0e6de3e59af9?w=400",
            "Accesorios", 200, false));
        productoRepo.save(new Producto(null, "Balón Oficial Entrenamiento",
            "Balón de fútbol sala oficial de los entrenamientos del equipo. Talla 4.",
            new java.math.BigDecimal("28.00"),
            "https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=400",
            "Equipamiento", 15, true));
        productoRepo.save(new Producto(null, "Mochila Albalate FS",
            "Mochila deportiva con compartimentos y escudo del club. 30L de capacidad.",
            new java.math.BigDecimal("40.00"),
            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400",
            "Equipamiento", 25, true));
        productoRepo.save(new Producto(null, "Taza Albalate FS",
            "Taza de cerámica con el escudo y colores del club. 330ml.",
            new java.math.BigDecimal("8.00"),
            "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=400",
            "Accesorios", 80, false));
        productoRepo.save(new Producto(null, "Calcetines Técnicos (pack x3)",
            "Pack de 3 pares de calcetines técnicos con el logo del club. Tallas 36-46.",
            new java.math.BigDecimal("10.00"),
            "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=400",
            "Ropa", 40, false));
        System.out.println("✅ 10 productos de tienda insertados.");
    }

    private LocalDateTime ldt(int y, int mo, int d, int h, int min) {
        return LocalDateTime.of(y, mo, d, h, min);
    }

    @SuppressWarnings("deprecation")
    private Date date(int y, int mo, int d) {
        return new Date(y - 1900, mo - 1, d);
    }
}
