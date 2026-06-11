# GrieferGames Addon by TMB

Dieses Addon wurde speziell für das GrieferGames 1.8-Netzwerk entwickelt und bietet zahlreiche nützliche Funktionen, um deinen Alltag auf dem Server zu optimieren.

## Automatisierung & Tools

### Autokomprimierer
Drücke bei geschlossenem Inventar die Pfeiltasten **LINKS + RECHTS + OBEN** gleichzeitig, um das Komprimieren-Menü zu öffnen. Das Item in deinem ersten Hotbar-Slot wird als Basis verwendet. Mit den Pfeiltasten **HOCH** und **RUNTER** kannst du zwischen zwei Listen wählen:
* **Liste #1 (Standard):** Komprimiert die Stufen I bis V und beginnt dann wieder von vorn.
* **Liste #2 (Schnell):** Optimiert für extrem große Item-Mengen durch häufigeres Komprimieren der unteren Stufen.

### Autodekomprimierer
Drücke bei geschlossenem Inventar die Pfeiltasten **LINKS + RECHTS + UNTEN** gleichzeitig. Das Item in deinem ersten Hotbar-Slot wird automatisch schrittweise dekomprimiert und auf den Boden geworfen.

### Autocrafter (V1 – V3)
* **V1:** Öffne mit `/craft` das Menü, lege das Rezept fest und speichere es mit **SHIFT + ENTER**. Mit **ENTER** startest du den Prozess. Unterstützt Endlosmodus und automatisches Droppen.
* **V2:** Wesentlich schneller als V1. Lege das Ziel-Item in den ersten Slot deiner Hotbar und nutze `/autocraft`. Das Addon nutzt das `/rezepte`-Menü, komprimiert automatisch und craftet weiter. Ideal in Verbindung mit Werferanlagen.
* **V3 (Effektivster):** Nutze `/craftV3`. Das Addon erfasst Materialien aus Truhen oder endlosen Lagern in deiner Nähe und craftet vollautomatisch.

### Kampf-Hilfen (HABK & VABK)
* **Halbautomatische Bonzeklinge (HABK):** Wechselt bei Schuss automatisch zwischen Bogen (Slot 3) und Klinge (Slot 1).
* **Vollautomatische Bonzeklinge (VABK):** Aktivierung über **SHIFT + V**. Das Addon spannt den Bogen, schießt und wechselt zur Klinge. Spannzeit und Wechsel-Cooldowns sind individuell konfigurierbar.

### Weitere Utilities
* **Auto Angler:** Zieht die Angel automatisch ein/aus und sortiert Loot direkt in EC oder Inventar.
* **InfinityMiner:** Hält für dich die linke Maustaste gedrückt (stoppt bei Werkzeugbruch).
* **Plot-Funktionen:** Plot-Grenzen grafisch anzeigen, **PlotWheel** für schnellen CB/GS-Wechsel und automatisches Annehmen von `/tpa`-Anfragen zwischen eigenen Accounts.
* **Automatische Trichtereinstellungen:** Speichere Filter, Radius und Stackgrößen, um Trichter beim Öffnen automatisch zu konfigurieren.
* **Optische Trichterhilfe:** Zeigt Verbindungen und Reichweiten (30 Blöcke) per Linien und Sphären an.
* **Auto Loot:** Holt automatisch `/freekiste` und `/grieferboost` ab und erinnert an `/kopf`.

---

## Chatfunktionen

### Kommunikation
* **Validierung von Zahlungen:** Erkennt Fake-Money bei eingehenden Zahlungen.
* **`/msg` in Tabs:** Jeder Chatpartner erhält einen eigenen Reiter. Dies verhindert das Senden an falsche Personen und macht das manuelle Voranstellen von `/r` oder `/msg` überflüssig.
* **Aufteilung von `/msg`:** Nachrichten über 100 Zeichen werden automatisch aufgeteilt.
* **Autokorrektur:** Korrigiert häufige Tippfehler (z. B. `7msg` zu `/msg`).
* **Entfernung von Leerzeilen:** Hält den Chat übersichtlich und umgeht `/clearchat`.

### Filter & Blocker
* **Cooldown-Benachrichtigungen:** Informiert dich, sobald Befehle wie `/rand` oder `/sign` wieder bereit sind.
* **News- & Streamer-Blocker:** Blendet Server-News oder Live-Benachrichtigungen aus.
* **Chat Cleaner:** Entfernt systemseitige Meldungen ohne Mehrwert.

---

## Befehlsübersicht

| Befehl | Funktion |
| :--- | :--- |
| `/dks <zahl>` | Berechnet benötigte Items/Stufen für X Doppelkisten. |
| `/pay ** <betrag>` | Zahlt Betrag an alle Spieler auf dem aktuellen CB. |
| `/fahndung <name>` | Sucht automatisch den CB eines Spielers ab. |
| `/auswurf` | Leert endlose Lager automatisch auf den Boden. |
| `/p t <name>` | Vereinfachte Schreibweise für `/p trust`. |
| `/cb<nummer>` | Schnellwechsel zu einem bestimmten CityBuild. |

---

## Installation
Lade die aktuelle `.jar`-Datei herunter und verschiebe sie in den entsprechenden Ordner:

| Betriebssystem | Pfad |
| :--- | :--- |
| **Windows** | `%APPDATA%\.minecraft\labymod-neo\addons` |
| **Linux** | `~/.minecraft/labymod-neo/addons` |
| **macOS** | `~/Library/Application Support/minecraft/labymod-neo/addons` |

***

*Hinweis: Für Funktionen wie den Itemschutz übernehmen wir keine Haftung. Nutze Automatisierungen stets mit Rücksicht auf die Serverleistung.*