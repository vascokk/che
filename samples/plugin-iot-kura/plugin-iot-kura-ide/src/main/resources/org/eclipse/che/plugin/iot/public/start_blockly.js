function startBlockly() {
    var toolbox = '<xml>';
    toolbox += '  <block type="controls_if"></block>';
    toolbox += '  <block type="controls_whileUntil"></block>';
    toolbox += '  <block type="logic_compare"></block>';
    toolbox += '  <block type="math_number"></block>';
    toolbox += '  <block type="math_arithmetic"></block>';
    toolbox += '  <block type="text"></block>';
    toolbox += '  <block type="text_print"></block>';
    toolbox += '</xml>';

    var blocklyDiv = document.getElementById('blocklyDiv');
    var workspace = Blockly.inject(blocklyDiv, {toolbox: toolbox});

    window.addEventListener('resize', onresize, false);
    onresize();
    Blockly.svgResize(workspace);
}

function startBlocklyResizable() {

    var toolbox = '<xml>';
    toolbox += '  <block type="controls_if"></block>';
    toolbox += '  <block type="controls_whileUntil"></block>';
    toolbox += '  <block type="logic_compare"></block>';
    toolbox += '  <block type="math_number"></block>';
    toolbox += '  <block type="math_arithmetic"></block>';
    toolbox += '  <block type="text"></block>';
    toolbox += '  <block type="text_print"></block>';
    toolbox += '</xml>';

  var blocklyArea = document.getElementById('blocklyArea');
  var blocklyDiv = document.getElementById('blocklyDiv');
  var workspace = Blockly.inject(blocklyDiv, {toolbox: toolbox});

  var onresize = function(e) {
    // Compute the absolute coordinates and dimensions of blocklyArea.
    // var element = blocklyArea;
    var element = blocklyArea;
    var x = 0;
    var y = 0;
    x += element.offsetLeft;
    y += element.offsetTop;
    element = element.offsetParent;
    x += element.offsetLeft;
    y += element.offsetTop;

    blocklyDiv.style.left = x + 'px';
    blocklyDiv.style.top = y + 'px';
    blocklyDiv.style.width = blocklyArea.offsetWidth + 'px';
    blocklyDiv.style.height = blocklyArea.offsetHeight + 'px';
  };
  window.addEventListener('resize', onresize, false);
  onresize();
  Blockly.svgResize(workspace);
}