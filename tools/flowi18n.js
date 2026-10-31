// Writes the "flow" translations into de_de.json and en_us.json (replacing the existing "flow" block).
const fs = require('fs');
const dir = process.argv[2];

// [id, de name, en name, {param: [de, en]}]
const nodes = [
  ['start', 'Start', 'Start', {}],
  ['on_inventory_full', 'Bei vollem Inventar', 'On full inventory', {}],
  ['on_chat', 'Bei Chatnachricht', 'On chat message', {contains: ['Enthält', 'Contains']}],
  ['on_menu_open', 'Bei Menü geöffnet', 'On menu opened', {title: ['Titel', 'Title']}],
  ['on_cb_change', 'Bei CB-Wechsel', 'On CB change', {cb: ['CB (leer = alle)', 'CB (empty = any)']}],
  ['on_key', 'Bei Taste', 'On key', {key: ['Taste(n), z. B. L_SHIFT+K', 'Key(s), e.g. L_SHIFT+K'], setting: ['oder Hotkey-Einstellung', 'or hotkey setting']}],
  ['on_mouse', 'Bei Maustaste', 'On mouse button', {button: ['Taste (LEFT/RIGHT)', 'Button (LEFT/RIGHT)'], action: ['Aktion (CLICK/RELEASE)', 'Action (CLICK/RELEASE)']}],
  ['on_chat_sent', 'Bei gesendetem Befehl', 'On sent message', {contains: ['Enthält', 'Contains']}],
  ['on_fish_bite', 'Wenn Fisch beißt', 'On fish bite', {}],
  ['on_timer', 'Alle X Ticks', 'Every X ticks', {ticks: ['Ticks', 'Ticks']}],
  ['command', 'Befehl senden', 'Send command', {command: ['Befehl', 'Command']}],
  ['chat', 'Chat schreiben', 'Write chat', {text: ['Text', 'Text']}],
  ['message', 'Nachricht anzeigen', 'Show message', {text: ['Text', 'Text']}],
  ['look_at', 'Block ansehen', 'Look at block', {x: ['X', 'X'], y: ['Y', 'Y'], z: ['Z', 'Z']}],
  ['look_direction', 'Blickrichtung', 'Look direction', {yaw: ['Yaw', 'Yaw'], pitch: ['Pitch', 'Pitch']}],
  ['right_click_block', 'Block rechtsklicken', 'Right click block', {}],
  ['left_click_block', 'Block linksklicken', 'Left click block', {}],
  ['break_block', 'Block abbauen', 'Break block', {ticks: ['Dauer (Ticks)', 'Duration (ticks)']}],
  ['use_item', 'Item benutzen', 'Use item', {}],
  ['release_item', 'Item loslassen', 'Release item', {}],
  ['select_slot', 'Hotbar-Slot wählen', 'Select hotbar slot', {slot: ['Slot (1-9)', 'Slot (1-9)']}],
  ['hold_key', 'Taste halten', 'Hold key', {key: ['Taste (sneak, jump, …)', 'Key (sneak, jump, …)'], pressed: ['Gedrückt', 'Pressed']}],
  ['click_slot', 'Menü-Slot klicken', 'Click menu slot', {slot: ['Slot', 'Slot'], shift: ['Shift', 'Shift']}],
  ['drop_slot', 'Slot wegwerfen', 'Drop slot', {slot: ['Slot', 'Slot']}],
  ['wait_for_clicks', 'Auf Klicks warten', 'Wait for clicks', {}],
  ['close_menu', 'Menü schließen', 'Close menu', {}],
  ['open_inventory', 'Inventar öffnen', 'Open inventory', {}],
  ['wait_for_menu', 'Auf Menü warten', 'Wait for menu', {title: ['Titel', 'Title'], timeout: ['Timeout (Ticks)', 'Timeout (ticks)']}],
  ['if_menu', 'Wenn Menü offen', 'If menu open', {title: ['Titel', 'Title']}],
  ['if_screen', 'Wenn Bildschirm', 'If screen', {screen: ['Bildschirm', 'Screen']}],
  ['if_slot', 'Wenn Slot enthält', 'If slot contains', {slot: ['Slot', 'Slot'], item: ['Item', 'Item']}],
  ['navigate_page', 'Menüseite ansteuern', 'Navigate to page', {indicator: ['Anzeige-Slot', 'Indicator slot'], prefix: ['Präfix', 'Prefix'], target: ['Zielseite', 'Target page'], previous: ['Zurück-Slot', 'Previous slot'], next: ['Weiter-Slot', 'Next slot'], timeout: ['Timeout (Ticks)', 'Timeout (ticks)']}],
  ['find_slot', 'Slot suchen', 'Find slot', {item: ['Item', 'Item'], variable: ['Variable', 'Variable'], full_stack: ['Nur volle Stacks', 'Full stacks only'], area: ['Bereich (inventory/chest/all)', 'Area (inventory/chest/all)'], slots: ['Slots (z. B. 0-24)', 'Slots (e.g. 0-24)'], min_level: ['Min. Komprimierung', 'Min. compression'], max_level: ['Max. Komprimierung', 'Max. compression'], named: ['Umbenannt (yes/no)', 'Renamed (yes/no)'], exclude: ['Außer', 'Except'], amount: ['Genaue Anzahl (0 = egal)', 'Exact amount (0 = any)']}],
  ['count_items', 'Items zählen', 'Count items', {item: ['Item', 'Item'], variable: ['Variable', 'Variable'], area: ['Bereich (inventory/chest/all)', 'Area (inventory/chest/all)'], slots: ['Slots (z. B. 0-24)', 'Slots (e.g. 0-24)'], min_level: ['Min. Komprimierung', 'Min. compression'], max_level: ['Max. Komprimierung', 'Max. compression'], named: ['Umbenannt (yes/no)', 'Renamed (yes/no)'], exclude: ['Außer', 'Except'], amount: ['Genaue Anzahl (0 = egal)', 'Exact amount (0 = any)']}],
  ['drop_items', 'Items wegwerfen', 'Drop items', {item: ['Item', 'Item'], keep_one: ['Einen behalten', 'Keep one'], area: ['Bereich (inventory/chest/all)', 'Area (inventory/chest/all)'], slots: ['Slots (z. B. 0-24)', 'Slots (e.g. 0-24)'], min_level: ['Min. Komprimierung', 'Min. compression'], max_level: ['Max. Komprimierung', 'Max. compression'], named: ['Umbenannt (yes/no)', 'Renamed (yes/no)'], exclude: ['Außer', 'Except'], amount: ['Genaue Anzahl (0 = egal)', 'Exact amount (0 = any)']}],
  ['move_items', 'Items verschieben (Shift)', 'Move items (shift)', {item: ['Item', 'Item'], max_stacks: ['Max. Stacks (0 = alle)', 'Max. stacks (0 = all)'], full_only: ['Nur volle Stacks', 'Full stacks only'], area: ['Bereich (inventory/chest/all)', 'Area (inventory/chest/all)'], slots: ['Slots (z. B. 0-24)', 'Slots (e.g. 0-24)'], min_level: ['Min. Komprimierung', 'Min. compression'], max_level: ['Max. Komprimierung', 'Max. compression'], named: ['Umbenannt (yes/no)', 'Renamed (yes/no)'], exclude: ['Außer', 'Except'], amount: ['Genaue Anzahl (0 = egal)', 'Exact amount (0 = any)']}],
  ['if_inventory_full', 'Wenn Inventar voll', 'If inventory full', {}],
  ['if_holding', 'Wenn in der Hand', 'If holding', {item: ['Item', 'Item']}],
  ['if_durability', 'Wenn Haltbarkeit', 'If durability', {min: ['Mindestens', 'At least']}],
  ['wait', 'Warten', 'Wait', {ticks: ['Ticks', 'Ticks']}],
  ['wait_for_key', 'Auf Taste warten', 'Wait for key', {key: ['Taste(n), z. B. L_SHIFT+K', 'Key(s), e.g. L_SHIFT+K'], timeout: ['Timeout (Ticks)', 'Timeout (ticks)']}],
  ['wait_for_chat', 'Auf Chat warten', 'Wait for chat', {contains: ['Enthält', 'Contains'], timeout: ['Timeout (Ticks)', 'Timeout (ticks)'], variable: ['Variable', 'Variable']}],
  ['repeat', 'Wiederholen', 'Repeat', {count: ['Anzahl', 'Count']}],
  ['if_compare', 'Vergleichen', 'Compare', {a: ['Wert A', 'Value A'], operator: ['Operator', 'Operator'], b: ['Wert B', 'Value B']}],
  ['run_flow', 'Ablauf/Funktion starten', 'Start flow/function', {flow: ['Name (z. B. AutoComp, CRAFTV3)', 'Name (e.g. AutoComp, CRAFTV3)'], wait: ['Warten bis fertig', 'Wait until finished']}],
  ['stop_flow', 'Ablauf/Funktion stoppen', 'Stop flow/function', {flow: ['Name', 'Name']}],
  ['stop', 'Stopp', 'Stop', {}],
  ['set_variable', 'Variable setzen', 'Set variable', {name: ['Name', 'Name'], value: ['Wert', 'Value']}],
  ['calculate', 'Rechnen', 'Calculate', {name: ['Variable', 'Variable'], operator: ['Operator', 'Operator'], value: ['Wert', 'Value']}],
  ['store_looked_block', 'Angesehenen Block merken', 'Store looked at block', {name: ['Name', 'Name']}],
  ['store_slot', 'Slot-Inhalt merken', 'Store slot content', {slot: ['Slot', 'Slot'], name: ['Name', 'Name']}],
  ['store_position', 'Position merken', 'Store position', {name: ['Name', 'Name']}],
  ['store_info', 'Spielinfos merken', 'Store game info', {name: ['Name', 'Name']}],
  ['split_text', 'Text aufteilen', 'Split text', {text: ['Text', 'Text'], separator: ['Trennzeichen', 'Separator'], name: ['Name', 'Name']}],
  ['extract_text', 'Text ausschneiden', 'Extract text', {text: ['Text', 'Text'], after: ['Nach', 'After'], before: ['Vor', 'Before'], name: ['Name', 'Name']}],
  ['parse_date', 'Datum lesen', 'Parse date', {text: ['Text', 'Text'], format: ['Format', 'Format'], name: ['Name', 'Name']}],
  ['if_player_online', 'Wenn Spieler online', 'If player online', {player: ['Spieler', 'Player']}],
];

const common = {
  de: {
    started: 'Ablauf §e%s §fgestartet.', stopped: 'Ablauf §e%s §fgestoppt.', finished: 'Ablauf §e%s §fbeendet.',
    noStart: 'Der Ablauf §e%s §fhat keinen verbundenen Start-Block.',
    unknown: 'Es gibt keinen Ablauf namens §e%s§f. Alle Abläufe: /flow list',
    noFlows: 'Du hast noch keine Abläufe. Öffne den Editor mit /flow',
    param: {autostart: 'Autostart'},
    editor: {newFlowName: 'Ablauf %s', hotkey: 'Hotkey:', save: 'Speichern', run: '▶ Starten', stop: '■ Stoppen', delete: 'Löschen',
      flows: 'Abläufe', newFlow: '+ Neuer Ablauf', deleteBlock: 'Block löschen', readOnly: 'Vorgefertigt, nicht veränderbar', copy: 'Als Kopie bearbeiten'},
    category: {trigger: 'Auslöser', action: 'Chat', player: 'Spieler', menu: 'Menüs', inventory: 'Inventar', logic: 'Logik', data: 'Variablen'},
    output: {next: 'weiter', yes: 'ja', no: 'nein', ok: 'ok', timeout: 'Timeout', loop: 'Schleife', done: 'fertig', found: 'gefunden', none: 'keiner', reached: 'erreicht'},
  },
  en: {
    started: 'Flow §e%s §fstarted.', stopped: 'Flow §e%s §fstopped.', finished: 'Flow §e%s §ffinished.',
    noStart: 'The flow §e%s §fhas no connected start block.',
    unknown: 'There is no flow named §e%s§f. All flows: /flow list',
    noFlows: 'You have no flows yet. Open the editor with /flow',
    param: {autostart: 'Autostart'},
    editor: {newFlowName: 'Flow %s', hotkey: 'Hotkey:', save: 'Save', run: '▶ Run', stop: '■ Stop', delete: 'Delete',
      flows: 'Flows', newFlow: '+ New flow', deleteBlock: 'Delete block', readOnly: 'Built-in, read only', copy: 'Edit as copy'},
    category: {trigger: 'Triggers', action: 'Chat', player: 'Player', menu: 'Menus', inventory: 'Inventory', logic: 'Logic', data: 'Variables'},
    output: {next: 'next', yes: 'yes', no: 'no', ok: 'ok', timeout: 'timeout', loop: 'loop', done: 'done', found: 'found', none: 'none', reached: 'reached'},
  },
};

for (const [file, lang, index] of [['de_de.json', 'de', 1], ['en_us.json', 'en', 2]]) {
  const flow = JSON.parse(JSON.stringify(common[lang]));
  flow.node = {};
  for (const node of nodes) {
    const entry = {name: node[index]};
    const params = Object.entries(node[3]);
    if (params.length)
      entry.param = Object.fromEntries(params.map(([key, names]) => [key, names[index - 1]]));
    flow.node[node[0]] = entry;
  }

  const path = dir + '/' + file;
  const text = fs.readFileSync(path, 'utf8');
  const newline = text.includes('\r\n') ? '\r\n' : '\n';
  const start = text.indexOf('        "flow": {');
  const end = text.indexOf('        "commands": {');
  const block = '        "flow": ' + JSON.stringify(flow, null, 4).split('\n').join(newline + '        ') + ',' + newline;
  const result = text.slice(0, start) + block + text.slice(end);
  JSON.parse(result);
  fs.writeFileSync(path, result);
  console.log(file, 'ok', Object.keys(flow.node).length, 'blocks');
}
