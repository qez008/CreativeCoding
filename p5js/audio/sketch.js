var mic;
var fft;

function setup() {
  createCanvas(840, 892-8);
  background(25);
  
  mic = new p5.AudioIn();
  fft = new p5.FFT(0.2, 64);
  
  mic.start();
  fft.setInput(mic);
}


function draw() {
  background(25);

  //sinWave();
  donutCircle();
}

var sinProg = 0;
var tailProg = 0;
var tail = [];
function sinWave(){
  translate(0, height / 2);
  noStroke();
  fill(100);
  
  fft.analyze();
  var nrg = fft.getEnergy(6000, 20000);
  var vol = mic.getLevel() * 200;

  sinProg += map(nrg, 0, 256, 0.001, 0.2);
  tailProg += 1;

  var value = sinProg * 100;
  var y = sin(sinProg) * 100;
  var x = (value % (width + 200)) - 100
  

  circle(x, y, 15 + vol);

  if (tailProg > 1) {
    tail.unshift(new p5.Vector(x, y));
    tailProg -= 10;
  }
  
  if (tail.length > 10) tail.pop();
  for (var i = 0; i < tail.length; i++) {
    var section = tail[i];
    fill(100, map(i, 0, 10, 256, 0));
    circle(section.x, section.y, 15);
  }
}

function squarePipe() {
  translate(width /2, height / 2);
  noFill();
  strokeWeight(2);
  stroke(100);

  var w = 80;
  var h = 120;

  var squares = 30;
  for(var i = 0; i < squares; i++) {
    var x = map(i, 0, squares, -width / 2 - 100, width / 2 + 100);
    var y = 0;
    beginShape();
    vertex(x, y);
    vertex(x + w / 2, y + h / 2);
    vertex(x + w, y);
    vertex(x + w / 2, y - h / 2);
    vertex(x, y);
    endShape();
  }
}


var donutProgress = 0;

function donutCircle() {
  translate(width/2, height/2);
  stroke(100);
  noFill();

  var w = 100;
  var h = 200;
  var dist = 50; 
  var circles = 9;
  var diff = 1.1;
  var levels = ["bass", "lowMid", "mid", "highMid", "treble"]
  var colors = [ color(250, 10, 120), 
                 color(240, 10, 180), 
                 color(100, 10, 220), 
                 color(10, 100, 240), 
                 color(210, 10, 140) ];
  
  donutProgress += 0.02;
  fft.analyze();

  for (var i = 0; i < circles; i++) {
    
    var p = map(i, 0, circles, 0, PI);
    var l = cos(donutProgress + p) * dist;
    var k = sin(donutProgress + p);
    var e = fft.getEnergy(6000, 20000);
    //stroke(colors[i]);
    //strokeWeight(i+1);
    ellipse(l, 0, w + w/diff * k + e / diff, h + h/diff * k + e);
  }
}

function hexagon() {
  var array = fft.analyze();
  
  var len = 80;
  var rad = len * 0.8660254;
  var sides = 6;
  var steps = 16;
  var vec = new p5.Vector(rad ,len / 2);

  translate(width / 2, height / 2);
  fill(200);
  beginShape();
  var a = PI / 2;
  for(var i = 0; i < sides; i++) {
    a += TAU / sides;
    for (var j = 0; j < steps; j++) {
      
      vertex(vec.x, vec.y);

      var k = i * sides + j;
      var value = map(array[k], 0, 256, 1, 3)
      ;
      vec.x += cos(a) * len / steps * value;
      vec.y += sin(a) * len / steps * value;
    }
  }
  endShape();

  fill(100);
  ellipse(0, 0, 16);
}


function bars(spec) {
  
  strokeWeight(10);
  
  var k = 80;
  
  var from = color(250, 0, 80);
  var to = color(0, 80, 250);
  var step = 4

  for (var i = 0; i < spec.length; i += step) {
    var x = map(i, 0, spec.length -1, k, width - k);
    var y = -spec[i] * 0.4;
    
    from = lerpColor(from, to, 1 / spec.length * step);
    stroke(from);
    
    //ellipse(x, 400, 10, 10+y);
    line(x, 400, x, 400 + y);
    
  }
}

function fun(wave, c) {
  
  fill(c);
  beginShape();
  vertex(width - 50, 400);
  vertex(50, 400);
  for(var i = 0; i < wave.length; i += 15) {
   
    var x = map(i, 0, wave.length -1, 50, width - 50);
    var y = wave[i] * 200;
    
    vertex(x, y + height / 2);
  }
  
  endShape();
}


function spectrumBlob() {
  
  var spec = fft.analyze()
  circleSpectrum(spec.slice(50, 190), 106, 1, 0.8);
  circleSpectrum(spec.slice(100, 200), 100, 1.8, 0.9);
  circleSpectrum(spec.slice(120, 160), 112, 1.2, 0.7);
}


function circleSpectrum(spectrum, rad, weight, fac) {
  
  noFill();
  noStroke();
  
  fill(25);
  stroke(100, 100, 100, 200);
  strokeWeight(weight);
  
  for (var t = -1; t <= 1; t += 2) {
    beginShape();
    
    for (var i = 0; i <= PI; i += PI / 20) {
      var idx = floor(map(i, 0, PI, 0, spectrum.length - 1));
      
      var val = spectrum[idx];
      var imp = pow(val, fac);

      var x = sin(i) * (rad + imp) * t;
      var y = cos(i) * (rad + imp);

      vertex(x, y);
    }
    
    endShape();
  }
}


function circleOfCircles(waveform, rad) {
  
  noFill();
  noStroke();
  stroke(200, 100, 255, 100);
  fill(200, 100, 255, 100);
  
  var vol = mic.getLevel();
  var weight = pow(vol, 0.2) * 3
  
  strokeWeight(weight);

  for (var i = 0; i < waveform.length; i++) {
    
    var idx = map(i, 0, waveform.length - 1, 0, TAU);
    var val = waveform[i];
    
    var x = sin(idx) * rad;
    var y = cos(idx) * rad;
    
    ellipse(x, y, log(val) * val * 200);
  }
}