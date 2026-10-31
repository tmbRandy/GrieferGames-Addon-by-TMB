// Generates the big built-in flows (AutoCrafter V1/V3) from a compact description, because they are too large
// to maintain by hand. Usage: node tools/flowgen.js core/src/main/resources/assets/tmbgriefergames/flows
const fs = require('fs');

class FlowBuilder {
  constructor(name, options = {}) {
    this.data = { name, silent: !!options.silent, hotkey: [], nodes: [], connections: [] };
    if (options.hotkeySetting) this.data.hotkeySetting = options.hotkeySetting;
  }
  // n(id, type, params, column, row)
  n(id, type, params, column, row) {
    if (this.data.nodes.some(node => node.id === id)) throw new Error('duplicate id ' + id);
    const stringParams = {};
    for (const [key, value] of Object.entries(params || {})) stringParams[key] = String(value);
    this.data.nodes.push({ id, type, x: column * 170, y: row * 100, params: stringParams });
    return this;
  }
  // l('from.output', 'to') or l('from', 'to') for "next"
  l(from, to) {
    const [node, output] = from.includes('.') ? from.split('.') : [from, 'next'];
    this.data.connections.push({ from: node, output, to });
    return this;
  }
  chain(...ids) {
    for (let i = 0; i < ids.length - 1; i++) this.l(ids[i], ids[i + 1]);
    return this;
  }
  write(dir, file) {
    const ids = new Set(this.data.nodes.map(node => node.id));
    for (const connection of this.data.connections)
      if (!ids.has(connection.from) || !ids.has(connection.to)) throw new Error('unknown node in ' + JSON.stringify(connection));
    fs.writeFileSync(dir + '/' + file, JSON.stringify(this.data, null, 2) + '\n');
    console.log(file, this.data.nodes.length, 'nodes', this.data.connections.length, 'connections');
  }
}

const out = process.argv[2];

// Opens the gold ingot recipe the "gold block to ingot" setting asks for (9 ingots from a block, otherwise
// 1 from nuggets) in the "Minecraft-Rezepte" menu and continues with next. Ids start with prefix + "Gold" (or "gold" without prefix).
function goldRecipe(f, prefix, column, row, next) {
  const id = name => (prefix ? prefix + 'Gold' : 'gold') + name;
  f.n(id('Setting'), 'if_compare', { a: '${setting.autoCrafter.goldBlockToIngot}', operator: '=', b: 'true' }, column, row)
   .n(id('Block'), 'set_variable', { name: 'goldCount', value: 9 }, column + 1, row)
   .n(id('Nuggets'), 'set_variable', { name: 'goldCount', value: 1 }, column + 1, row + 1)
   .n(id('Find'), 'find_slot', { item: 'minecraft:gold_ingot', variable: 'goldSlot', area: 'chest', slots: '10-43', amount: '${goldCount}' }, column + 2, row)
   .n(id('Click'), 'click_slot', { slot: '${goldSlot}' }, column + 3, row)
   .n(id('Fallback'), 'click_slot', { slot: 53 }, column + 3, row + 1);
  f.l(id('Setting') + '.yes', id('Block')).l(id('Setting') + '.no', id('Nuggets'))
   .l(id('Block'), id('Find')).l(id('Nuggets'), id('Find'))
   .l(id('Find') + '.found', id('Click')).l(id('Find') + '.none', id('Fallback'))
   .l(id('Click'), next).l(id('Fallback'), next);
}

// ------------------------------------------------------------------ AutoCrafter V1
// Crafting table: 0 result, 1-9 grid, 10-36 inventory, 37-45 hotbar
{
  const R = 'global.autocrafter.v1';
  const f = new FlowBuilder('AutoCrafterV1', { silent: true });

  // SHIFT + ENTER saves the recipe that lies in the crafting grid (must be checked before plain ENTER)
  f.n('saveKey', 'on_key', { key: 'L_SHIFT+ENTER', autostart: false }, 0, 0)
   .n('saveInCraft', 'if_screen', { screen: 'crafting' }, 1, 0)
   .n('saveResult', 'store_slot', { slot: 0, name: `${R}.result` }, 2, 0)
   .n('noResult', 'message', { text: '§cAutoCrafter V1: Lege zuerst ein gültiges Rezept in das Craftingfeld.' }, 2, 1)
   .n('saveFirst', 'set_variable', { name: 'i', value: 1 }, 3, 0)
   .n('saveSlot', 'store_slot', { slot: '${i}', name: `${R}.\${i}` }, 4, 0)
   .n('saveNext', 'calculate', { name: 'i', operator: '+', value: 1 }, 5, 0)
   .n('saveDone', 'if_compare', { a: '${i}', operator: '>', b: 9 }, 6, 0)
   .n('saved', 'message', { text: 'AutoCrafter V1: Rezept gespeichert.' }, 7, 0);
  f.l('saveKey', 'saveInCraft').l('saveInCraft.yes', 'saveResult').l('saveInCraft.no', 'end')
   .l('saveResult.found', 'saveFirst').l('saveResult.none', 'noResult').l('noResult', 'end')
   .chain('saveFirst', 'saveSlot').l('saveSlot.found', 'saveNext').l('saveSlot.none', 'saveNext')
   .l('saveNext', 'saveDone').l('saveDone.yes', 'saved').l('saveDone.no', 'saveSlot').l('saved', 'end');

  // ENTER crafts once, in endless mode it toggles crafting on and off
  f.n('craftKey', 'on_key', { key: 'ENTER', autostart: false }, 0, 3)
   .n('craftInCraft', 'if_screen', { screen: 'crafting' }, 1, 3)
   .n('hasRecipe', 'if_compare', { a: `\${${R}.result.id}`, operator: '!=', b: '' }, 2, 3)
   .n('noRecipe', 'message', { text: '§cAutoCrafter V1: Speichere zuerst ein Rezept mit SHIFT + ENTER.' }, 2, 4)
   .n('endless', 'if_compare', { a: '${setting.autoCrafter.endlessMode}', operator: '=', b: 'true' }, 3, 3)
   .n('isRunning', 'if_compare', { a: '${global.autocrafter.v1.running}', operator: '=', b: '1' }, 4, 2)
   .n('stopRunning', 'set_variable', { name: 'global.autocrafter.v1.running', value: 0 }, 5, 1)
   .n('stoppedMessage', 'message', { text: 'AutoCrafter V1: Endlosmodus aus.' }, 6, 1)
   .n('startRunning', 'set_variable', { name: 'global.autocrafter.v1.running', value: 1 }, 5, 2)
   .n('startedMessage', 'message', { text: 'AutoCrafter V1: Endlosmodus an.' }, 6, 2)
   .n('autoDrop', 'if_compare', { a: '${setting.autoCrafter.autoDrop}', operator: '=', b: 'true' }, 4, 3)
   .n('dropOutput', 'drop_items', { item: `\${${R}.result.id}`, area: 'inventory' }, 5, 3)
   .n('clearGrid', 'move_items', { item: '', slots: '1-9' }, 6, 3)
   .n('fillFirst', 'set_variable', { name: 'i', value: 1 }, 7, 3)
   .n('slotNeeded', 'if_compare', { a: `\${${R}.\${i}.id}`, operator: '!=', b: '' }, 8, 3)
   .n('findIngredient', 'find_slot', { item: `\${${R}.\${i}.id}`, variable: 'source', area: 'inventory', full_stack: '${setting.autoCrafter.onlyFullStacks}' }, 9, 2)
   .n('pick', 'click_slot', { slot: '${source}', shift: false }, 10, 2)
   .n('put', 'click_slot', { slot: '${i}', shift: false }, 11, 2)
   .n('fillNext', 'calculate', { name: 'i', operator: '+', value: 1 }, 9, 4)
   .n('fillDone', 'if_compare', { a: '${i}', operator: '>', b: 9 }, 10, 4)
   .n('take', 'click_slot', { slot: 0, shift: true }, 11, 4)
   .n('again', 'if_compare', { a: '${global.autocrafter.v1.running}', operator: '=', b: '1' }, 12, 4)
   .n('againPause', 'wait', { ticks: 2 }, 12, 3)
   .n('missing', 'message', { text: '§cAutoCrafter V1: Es fehlt Material.' }, 10, 1)
   .n('missingStop', 'set_variable', { name: 'global.autocrafter.v1.running', value: 0 }, 11, 1)
   .n('end', 'stop', {}, 13, 4);
  f.l('craftKey', 'craftInCraft').l('craftInCraft.yes', 'hasRecipe').l('craftInCraft.no', 'end')
   .l('hasRecipe.yes', 'endless').l('hasRecipe.no', 'noRecipe').l('noRecipe', 'end')
   .l('endless.yes', 'isRunning').l('endless.no', 'autoDrop')
   .l('isRunning.yes', 'stopRunning').l('stopRunning', 'stoppedMessage').l('stoppedMessage', 'end')
   .l('isRunning.no', 'startRunning').l('startRunning', 'startedMessage').l('startedMessage', 'autoDrop')
   .l('autoDrop.yes', 'dropOutput').l('autoDrop.no', 'clearGrid').l('dropOutput', 'clearGrid')
   .chain('clearGrid', 'fillFirst', 'slotNeeded')
   .l('slotNeeded.yes', 'findIngredient').l('slotNeeded.no', 'fillNext')
   .l('findIngredient.found', 'pick').l('findIngredient.none', 'missing').chain('pick', 'put', 'fillNext')
   .l('fillNext', 'fillDone').l('fillDone.yes', 'take').l('fillDone.no', 'slotNeeded')
   .l('take', 'again').l('again.yes', 'againPause').l('again.no', 'end').l('againPause', 'autoDrop')
   .chain('missing', 'missingStop', 'end');
  f.write(out, 'autocrafterv1.json');
}

// ------------------------------------------------------------------ AutoCrafter V3
// /rezepte sub menus are double chests: player inventory starts at 54, hotbar slot 1 is 81
{
  const f = new FlowBuilder('AutoCrafterV3');

  // A: read the recipe of the item in hotbar slot 1
  f.n('start', 'start', {}, 0, 0)
   .n('craftItem', 'store_slot', { slot: 36, name: 'craft' }, 1, 0)
   .n('noItem', 'message', { text: '§cAutoCrafter V3: Lege das zu craftende Item in deinen ersten Hotbar-Slot.' }, 1, 1)
   .n('openRezepte', 'command', { command: '/rezepte' }, 2, 0)
   .n('waitMain', 'wait_for_menu', { title: '=Custom-Kategorien', timeout: 100 }, 3, 0)
   .n('openRecipes', 'click_slot', { slot: 12 }, 4, 0)
   .n('waitRecipes', 'wait_for_menu', { title: '=Minecraft-Rezepte', timeout: 100 }, 5, 0)
   .n('isGold', 'if_compare', { a: '${craft.id}', operator: '=', b: 'minecraft:gold_ingot' }, 5, 1)
   .n('openRecipe', 'click_slot', { slot: 81 }, 6, 0)
   .n('waitRecipe', 'wait_for_menu', { title: '=Vanilla Bauanleitung', timeout: 100 }, 7, 0)
   .n('slotList', 'set_variable', { name: 'slotList', value: '10 11 12 19 20 21 28 29 30' }, 8, 0)
   .n('splitSlots', 'split_text', { text: '${slotList}', separator: ' ', name: 'grid' }, 9, 0)
   .n('noMaterials', 'set_variable', { name: 'materials', value: '' }, 10, 0)
   .n('readFirst', 'set_variable', { name: 'j', value: 1 }, 11, 0)
   .n('readSlot', 'store_slot', { slot: '${grid.${j}}', name: 'ingredient' }, 12, 0)
   .n('isGlass', 'if_compare', { a: '${ingredient.id}', operator: '=', b: 'minecraft:stained_glass_pane' }, 13, 0)
   .n('isNew', 'if_compare', { a: '${need.${ingredient.id}}', operator: '=', b: '' }, 14, 0)
   .n('firstMaterial', 'if_compare', { a: '${materials}', operator: '=', b: '' }, 15, 0)
   .n('setMaterials', 'set_variable', { name: 'materials', value: '${ingredient.id}' }, 16, -1)
   .n('addMaterial', 'set_variable', { name: 'materials', value: '${materials} ${ingredient.id}' }, 16, 1)
   .n('countNeed', 'calculate', { name: 'need.${ingredient.id}', operator: '+', value: 1 }, 17, 0)
   .n('maxStack', 'set_variable', { name: 'max.${ingredient.id}', value: '${ingredient.maxStack}' }, 17, 1)
   .n('readNext', 'calculate', { name: 'j', operator: '+', value: 1 }, 18, 0)
   .n('readDone', 'if_compare', { a: '${j}', operator: '>', b: 9 }, 19, 0)
   .n('closeRecipe', 'close_menu', {}, 20, 0)
   .n('anyMaterial', 'if_compare', { a: '${materials}', operator: '!=', b: '' }, 21, 0)
   .n('splitMaterials', 'split_text', { text: '${materials}', separator: ' ', name: 'mat' }, 22, 0)
   .n('recipeSaved', 'message', { text: 'AutoCrafter V3: Rezept gespeichert (${mat.count} Materialien).' }, 23, 0);
  f.chain('start', 'craftItem').l('craftItem.found', 'openRezepte').l('craftItem.none', 'noItem').l('noItem', 'end')
   .l('openRezepte', 'waitMain').l('waitMain.ok', 'openRecipes').l('waitMain.timeout', 'menuError')
   .l('openRecipes', 'waitRecipes').l('waitRecipes.ok', 'isGold').l('waitRecipes.timeout', 'menuError')
   .l('isGold.yes', 'goldSetting').l('isGold.no', 'openRecipe')
   .l('openRecipe', 'waitRecipe').l('waitRecipe.ok', 'variantCheck').l('waitRecipe.timeout', 'noRecipe')
   .chain('slotList', 'splitSlots', 'noMaterials', 'readFirst', 'readSlot')
   .l('readSlot.found', 'isGlass').l('readSlot.none', 'readNext')
   .l('isGlass.yes', 'readNext').l('isGlass.no', 'isNew')
   .l('isNew.yes', 'firstMaterial').l('isNew.no', 'countNeed')
   .l('firstMaterial.yes', 'setMaterials').l('firstMaterial.no', 'addMaterial')
   .l('setMaterials', 'countNeed').l('addMaterial', 'countNeed').chain('countNeed', 'maxStack', 'readNext', 'readDone')
   .l('readDone.yes', 'closeRecipe').l('readDone.no', 'readSlot')
   .l('closeRecipe', 'anyMaterial').l('anyMaterial.yes', 'splitMaterials').l('anyMaterial.no', 'noRecipe')
   .chain('splitMaterials', 'recipeSaved', 'chestFirst');

  // Gold ingots have two recipes (nuggets / gold block), the setting decides which one is crafted
  goldRecipe(f, '', 6, 1, 'waitRecipe');

  // Items with several recipes show "Variante X" in slot 49: the player picks one and confirms with ENTER
  f.n('variantCheck', 'store_slot', { slot: 49, name: 'indicator' }, 8, -2)
   .n('isVariant', 'if_compare', { a: '${indicator.id}', operator: '=', b: 'minecraft:skull' }, 9, -2)
   .n('askVariant', 'message', { text: 'AutoCrafter V3: Wähle die Rezept-Variante und drücke §eENTER§f.' }, 10, -3)
   .n('waitVariant', 'wait_for_key', { key: 'ENTER', timeout: 2400 }, 11, -3)
   .n('readVariant', 'store_slot', { slot: 49, name: 'indicator' }, 12, -3)
   .n('variantNumber', 'extract_text', { text: '${indicator.name}', after: 'Variante ', before: '', name: 'variant' }, 13, -3)
   .n('noVariant', 'set_variable', { name: 'variant', value: '' }, 10, -1);
  f.l('variantCheck.found', 'isVariant').l('variantCheck.none', 'noVariant')
   .l('isVariant.yes', 'askVariant').l('isVariant.no', 'noVariant').l('askVariant', 'waitVariant')
   .l('waitVariant.ok', 'readVariant').l('waitVariant.timeout', 'end')
   .l('readVariant.found', 'variantNumber').l('readVariant.none', 'noVariant')
   .l('variantNumber.found', 'slotList').l('variantNumber.none', 'noVariant').l('noVariant', 'slotList');

  // B: the player opens one source chest per material
  f.n('chestFirst', 'set_variable', { name: 'k', value: 1 }, 0, 3)
   .n('askChest', 'message', { text: 'AutoCrafter V3: Öffne die Kiste mit §e${mat.${k}}§f.' }, 1, 3)
   .n('waitChest', 'wait_for_menu', { title: '', timeout: 2400 }, 2, 3)
   .n('countInChest', 'count_items', { item: '${mat.${k}}', area: 'chest', variable: 'inChest' }, 3, 3)
   .n('chestHasIt', 'if_compare', { a: '${inChest}', operator: '>', b: 0 }, 4, 3)
   .n('wrongChest', 'message', { text: '§cDiese Kiste enthält kein ${mat.${k}}.' }, 5, 4)
   .n('closeWrong', 'close_menu', {}, 6, 4)
   .n('rememberChest', 'store_looked_block', { name: 'chest.${mat.${k}}' }, 5, 3)
   .n('closeChest', 'close_menu', {}, 6, 3)
   .n('chestNext', 'calculate', { name: 'k', operator: '+', value: 1 }, 7, 3)
   .n('chestsDone', 'if_compare', { a: '${k}', operator: '>', b: '${mat.count}' }, 8, 3);
  f.chain('chestFirst', 'askChest', 'waitChest').l('waitChest.ok', 'countInChest').l('waitChest.timeout', 'end')
   .l('countInChest', 'chestHasIt').l('chestHasIt.yes', 'rememberChest').l('chestHasIt.no', 'wrongChest')
   .chain('wrongChest', 'closeWrong', 'askChest')
   .l('rememberChest.found', 'closeChest').l('rememberChest.none', 'closeWrong')
   .chain('closeChest', 'chestNext', 'chestsDone').l('chestsDone.no', 'askChest').l('chestsDone.yes', 'totalFirst');

  // C: how many crafts fit into the inventory. Every material weighs 64 / max. stack size per item (an egg 4),
  // the estimate is then lowered until the rounded up stacks of all materials fit into the free slots.
  f.n('totalFirst', 'set_variable', { name: 'total', value: 0 }, 9, 3)
   .n('totalIndex', 'set_variable', { name: 'k', value: 1 }, 10, 3)
   .n('weight', 'set_variable', { name: 'w', value: '${need.${mat.${k}}}' }, 11, 3)
   .n('weightStacks', 'calculate', { name: 'w', operator: '*', value: 64 }, 12, 3)
   .n('weightMax', 'calculate', { name: 'w', operator: '/', value: '${max.${mat.${k}}}' }, 13, 3)
   .n('totalAdd', 'calculate', { name: 'total', operator: '+', value: '${w}' }, 14, 3)
   .n('totalNext', 'calculate', { name: 'k', operator: '+', value: 1 }, 15, 3)
   .n('totalDone', 'if_compare', { a: '${k}', operator: '>', b: '${mat.count}' }, 16, 3)
   .n('isComp', 'if_compare', { a: '${setting.autoCrafter.finalActionV3}', operator: '=', b: 'COMP' }, 17, 3)
   .n('slotsComp', 'set_variable', { name: 'slots', value: 26 }, 18, 2)
   .n('slotsDrop', 'set_variable', { name: 'slots', value: 34 }, 18, 4)
   .n('craftsSlots', 'set_variable', { name: 'crafts', value: '${slots}' }, 19, 3)
   .n('craftsStacks', 'calculate', { name: 'crafts', operator: '*', value: 64 }, 20, 3)
   .n('craftsDivide', 'calculate', { name: 'crafts', operator: '/', value: '${total}' }, 21, 3)
   .n('checkFirst', 'set_variable', { name: 'used', value: 0 }, 9, 1)
   .n('checkIndex', 'set_variable', { name: 'k', value: 1 }, 10, 1)
   .n('checkMaterial', 'set_variable', { name: 'm', value: '${mat.${k}}' }, 11, 1)
   .n('checkAmount', 'set_variable', { name: 't', value: '${crafts}' }, 12, 1)
   .n('checkNeed', 'calculate', { name: 't', operator: '*', value: '${need.${m}}' }, 13, 1)
   .n('checkRound', 'calculate', { name: 't', operator: '+', value: '${max.${m}}' }, 14, 1)
   .n('checkRoundDown', 'calculate', { name: 't', operator: '-', value: 1 }, 15, 1)
   .n('checkStacks', 'calculate', { name: 't', operator: '/', value: '${max.${m}}' }, 16, 1)
   .n('checkUsed', 'calculate', { name: 'used', operator: '+', value: '${t}' }, 17, 1)
   .n('checkNext', 'calculate', { name: 'k', operator: '+', value: 1 }, 18, 1)
   .n('checkDone', 'if_compare', { a: '${k}', operator: '>', b: '${mat.count}' }, 19, 1)
   .n('tooMany', 'if_compare', { a: '${used}', operator: '>', b: '${slots}' }, 20, 1)
   .n('craftsLess', 'calculate', { name: 'crafts', operator: '-', value: 1 }, 21, 0)
   .n('craftsLeft', 'if_compare', { a: '${crafts}', operator: '<', b: 1 }, 22, 0)
   .n('doesNotFit', 'message', { text: '§cAutoCrafter V3: Das Material für einen Craft passt nicht ins Inventar.' }, 23, 0)
   .n('started', 'message', { text: 'AutoCrafter V3: Starte, ${crafts} Crafts pro Runde.' }, 22, 1);
  f.chain('totalFirst', 'totalIndex', 'weight', 'weightStacks', 'weightMax', 'totalAdd', 'totalNext', 'totalDone')
   .l('totalDone.no', 'weight').l('totalDone.yes', 'isComp')
   .l('isComp.yes', 'slotsComp').l('isComp.no', 'slotsDrop').l('slotsComp', 'craftsSlots').l('slotsDrop', 'craftsSlots')
   .chain('craftsSlots', 'craftsStacks', 'craftsDivide', 'checkFirst', 'checkIndex', 'checkMaterial', 'checkAmount', 'checkNeed',
     'checkRound', 'checkRoundDown', 'checkStacks', 'checkUsed', 'checkNext', 'checkDone')
   .l('checkDone.no', 'checkMaterial').l('checkDone.yes', 'tooMany')
   .l('tooMany.yes', 'craftsLess').l('tooMany.no', 'started').l('craftsLess', 'craftsLeft')
   .l('craftsLeft.yes', 'doesNotFit').l('craftsLeft.no', 'checkFirst').l('doesNotFit', 'end')
   .l('started', 'collectFirst');

  // D: take the missing materials out of their chests
  f.n('collectFirst', 'set_variable', { name: 'k', value: 1 }, 0, 6)
   .n('material', 'set_variable', { name: 'm', value: '${mat.${k}}' }, 1, 6)
   .n('target', 'set_variable', { name: 'target', value: '${crafts}' }, 2, 6)
   .n('targetNeed', 'calculate', { name: 'target', operator: '*', value: '${need.${m}}' }, 3, 6)
   .n('targetRound', 'calculate', { name: 'target', operator: '+', value: '${max.${m}}' }, 4, 6)
   .n('targetRoundDown', 'calculate', { name: 'target', operator: '-', value: 1 }, 4, 7)
   .n('targetStacks', 'calculate', { name: 'target', operator: '/', value: '${max.${m}}' }, 5, 6)
   .n('countHave', 'count_items', { item: '${m}', area: 'inventory', variable: 'have' }, 6, 6)
   .n('enough', 'if_compare', { a: '${have}', operator: '>=', b: '${target}' }, 7, 6)
   .n('missingStacks', 'set_variable', { name: 'missing', value: '${target}' }, 8, 7)
   .n('missingCalc', 'calculate', { name: 'missing', operator: '-', value: '${have}' }, 9, 7)
   .n('lookAtChest', 'look_at', { x: '${chest.${m}.x}', y: '${chest.${m}.y}', z: '${chest.${m}.z}' }, 10, 7)
   .n('lookPause', 'wait', { ticks: 2 }, 11, 8)
   .n('openSource', 'right_click_block', {}, 11, 7)
   .n('waitSource', 'wait_for_menu', { title: '', timeout: 60 }, 12, 7)
   .n('countSource', 'count_items', { item: '${m}', area: 'chest', variable: 'inSource' }, 13, 7)
   .n('sourceEmpty', 'if_compare', { a: '${inSource}', operator: '=', b: 0 }, 14, 7)
   .n('emptyCount', 'calculate', { name: 'empty', operator: '+', value: 1 }, 15, 8)
   .n('tooOftenEmpty', 'if_compare', { a: '${empty}', operator: '>=', b: 3 }, 16, 8)
   .n('emptyMessage', 'message', { text: '§cAutoCrafter V3: Die Kiste mit ${m} ist leer.' }, 17, 8)
   .n('resetEmpty', 'set_variable', { name: 'empty', value: 0 }, 15, 6)
   .n('takeStacks', 'move_items', { item: '${m}', area: 'chest', max_stacks: '${missing}', full_only: true }, 16, 6)
   .n('closeSource', 'close_menu', {}, 17, 6)
   .n('sourcePause', 'wait', { ticks: 5 }, 18, 6)
   .n('collectNext', 'calculate', { name: 'k', operator: '+', value: 1 }, 8, 5)
   .n('collectDone', 'if_compare', { a: '${k}', operator: '>', b: '${mat.count}' }, 9, 5);
  f.chain('collectFirst', 'material', 'target', 'targetNeed', 'targetRound', 'targetRoundDown', 'targetStacks', 'countHave', 'enough')
   .l('enough.yes', 'collectNext').l('enough.no', 'missingStacks')
   .chain('missingStacks', 'missingCalc', 'lookAtChest', 'lookPause', 'openSource', 'waitSource')
   .l('waitSource.ok', 'countSource').l('waitSource.timeout', 'emptyCount')
   .l('countSource', 'sourceEmpty').l('sourceEmpty.yes', 'emptyCount').l('sourceEmpty.no', 'resetEmpty')
   .l('emptyCount', 'tooOftenEmpty').l('tooOftenEmpty.yes', 'emptyMessage').l('tooOftenEmpty.no', 'closeSource').l('emptyMessage', 'end')
   .chain('resetEmpty', 'takeStacks', 'closeSource', 'sourcePause', 'material')
   .l('collectNext', 'collectDone').l('collectDone.no', 'material').l('collectDone.yes', 'craftOpen');

  // E: craft via /rezepte
  f.n('craftOpen', 'command', { command: '/rezepte' }, 10, 5)
   .n('craftWaitMain', 'wait_for_menu', { title: '=Custom-Kategorien', timeout: 100 }, 11, 5)
   .n('craftRecipes', 'click_slot', { slot: 12 }, 12, 5)
   .n('craftWaitRecipes', 'wait_for_menu', { title: '=Minecraft-Rezepte', timeout: 100 }, 13, 5)
   .n('craftIsGold', 'if_compare', { a: '${craft.id}', operator: '=', b: 'minecraft:gold_ingot' }, 14, 4.2)
   .n('craftHasVariant', 'if_compare', { a: '${variant}', operator: '!=', b: '' }, 19, 4.2)
   .n('craftVariant', 'navigate_page', { prefix: 'Variante ', target: '${variant}' }, 20, 4.2)
   .n('craftFind', 'find_slot', { item: '${craft.id}', variable: 'craftSlot', area: 'inventory' }, 14, 5)
   .n('craftChoose', 'click_slot', { slot: '${craftSlot}' }, 15, 5)
   .n('craftWaitRecipe', 'wait_for_menu', { title: '=Vanilla Bauanleitung', timeout: 100 }, 16, 5)
   .n('craftClick', 'click_slot', { slot: 52 }, 17, 5)
   .n('craftPause', 'wait', { ticks: 10 }, 18, 5)
   .n('craftClose', 'close_menu', {}, 19, 5)
   .n('countMade', 'count_items', { item: '${craft.id}', area: 'inventory', variable: 'made' }, 20, 5)
   .n('madeSome', 'if_compare', { a: '${made}', operator: '>', b: 1 }, 21, 5)
   .n('finalComp', 'if_compare', { a: '${setting.autoCrafter.finalActionV3}', operator: '=', b: 'COMP' }, 22, 5);
  f.l('craftOpen', 'craftWaitMain').l('craftWaitMain.ok', 'craftRecipes').l('craftWaitMain.timeout', 'menuError')
   .l('craftRecipes', 'craftWaitRecipes').l('craftWaitRecipes.ok', 'craftIsGold').l('craftWaitRecipes.timeout', 'menuError')
   .l('craftIsGold.yes', 'craftGoldSetting').l('craftIsGold.no', 'craftFind')
   .l('craftFind.found', 'craftChoose').l('craftFind.none', 'menuError')
   .l('craftChoose', 'craftWaitRecipe').l('craftWaitRecipe.ok', 'craftHasVariant').l('craftWaitRecipe.timeout', 'menuError')
   .l('craftHasVariant.yes', 'craftVariant').l('craftHasVariant.no', 'craftClick')
   .l('craftVariant.reached', 'craftClick').l('craftVariant.timeout', 'menuError')
   .chain('craftClick', 'craftPause', 'craftClose', 'countMade', 'madeSome')
   .l('madeSome.yes', 'finalComp').l('madeSome.no', 'collectFirst')
   .l('finalComp.yes', 'compOpen').l('finalComp.no', 'dropInventory');
  goldRecipe(f, 'craft', 15, 4.2, 'craftWaitRecipe');

  // F1: drop the crafted items (one stack stays as reference)
  f.n('dropInventory', 'open_inventory', {}, 22, 7)
   .n('dropPause', 'wait', { ticks: 10 }, 23, 7)
   .n('dropCrafted', 'drop_items', { item: '${craft.id}', keep_one: true, area: 'inventory' }, 24, 7)
   .n('dropClose', 'close_menu', {}, 25, 7);
  f.chain('dropInventory', 'dropPause', 'dropCrafted', 'dropClose', 'collectFirst');

  // F2: compress the crafted items up to level 6
  f.n('compOpen', 'command', { command: '/rezepte' }, 22, 3)
   .n('compWaitMain', 'wait_for_menu', { title: '=Custom-Kategorien', timeout: 100 }, 23, 3)
   .n('compChoose', 'click_slot', { slot: 11 }, 24, 3)
   .n('compWaitRecipes', 'wait_for_menu', { title: '=Item-Komprimierung-Bauanleitung', timeout: 100 }, 25, 3)
   .n('compFind', 'find_slot', { item: '${craft.id}', variable: 'compSlot', area: 'inventory', max_level: 0 }, 26, 3)
   .n('compClick', 'click_slot', { slot: '${compSlot}' }, 27, 3)
   .n('compWait', 'wait_for_menu', { title: '=Item-Komprimierung', timeout: 100 }, 28, 3)
   .n('compFirst', 'set_variable', { name: 'level', value: 1 }, 29, 3)
   .n('compGoto', 'navigate_page', { target: '${level}' }, 30, 3)
   .n('compConfirm', 'click_slot', { slot: 52 }, 31, 3)
   .n('compPause', 'wait', { ticks: 8 }, 32, 3)
   .n('compNext', 'calculate', { name: 'level', operator: '+', value: 1 }, 33, 3)
   .n('compDone', 'if_compare', { a: '${level}', operator: '>', b: 6 }, 34, 3)
   .n('compClose', 'close_menu', {}, 35, 3);
  f.l('compOpen', 'compWaitMain').l('compWaitMain.ok', 'compChoose').l('compWaitMain.timeout', 'menuError')
   .l('compChoose', 'compWaitRecipes').l('compWaitRecipes.ok', 'compFind').l('compWaitRecipes.timeout', 'menuError')
   .l('compFind.found', 'compClick').l('compFind.none', 'compClose')
   .l('compClick', 'compWait').l('compWait.ok', 'compFirst').l('compWait.timeout', 'menuError')
   .chain('compFirst', 'compGoto').l('compGoto.reached', 'compConfirm').l('compGoto.timeout', 'compClose')
   .chain('compConfirm', 'compPause', 'compNext', 'compDone').l('compDone.yes', 'compClose').l('compDone.no', 'compGoto')
   .l('compClose', 'collectFirst');

  // Errors and abort
  f.n('noRecipe', 'message', { text: '§cAutoCrafter V3: Für dieses Item wurde kein Rezept gefunden.' }, 2, 10)
   .n('menuError', 'message', { text: '§cAutoCrafter V3: Ein Menü hat nicht reagiert.' }, 3, 10)
   .n('escape', 'on_key', { key: 'ESCAPE', autostart: false }, 0, 10)
   .n('end', 'stop', {}, 4, 10);
  f.l('noRecipe', 'end').l('menuError', 'end').l('escape', 'end');
  f.write(out, 'autocrafterv3.json');
}
