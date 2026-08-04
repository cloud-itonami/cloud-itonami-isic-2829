// Urban Mining Cell v1 parametric layout model.
// Units: millimeters. Export to STL/DXF from OpenSCAD for review models.

cell_x = 6000;
cell_y = 3000;
guard_h = 2200;
frame_post = 40;
bin_w = 360;
bin_d = 420;
bin_h = 520;
bin_gap = 60;

module extrusion(length, axis = "x") {
  color([0.55, 0.58, 0.60])
    if (axis == "x") cube([length, frame_post, frame_post]);
    else if (axis == "y") cube([frame_post, length, frame_post]);
    else cube([frame_post, frame_post, length]);
}

module bin(label_index) {
  y = label_index * (bin_d + bin_gap);
  color([0.18, 0.25, 0.32])
    translate([0, y, 0]) cube([bin_w, bin_d, bin_h]);
  color([0.08, 0.45, 0.55])
    translate([-8, y + 20, bin_h - 80]) cube([8, bin_d - 40, 50]);
}

module guard_frame() {
  for (x = [0, cell_x - frame_post]) {
    for (y = [0, cell_y - frame_post]) {
      translate([x, y, 0]) extrusion(guard_h, "z");
    }
  }
  translate([0, 0, guard_h]) extrusion(cell_x, "x");
  translate([0, cell_y - frame_post, guard_h]) extrusion(cell_x, "x");
  translate([0, 0, guard_h]) extrusion(cell_y, "y");
  translate([cell_x - frame_post, 0, guard_h]) extrusion(cell_y, "y");
}

module inspection_tunnel() {
  color([0.12, 0.12, 0.14])
    translate([900, 1150, 700]) cube([500, 700, 520]);
  color([0.9, 0.85, 0.55])
    translate([930, 1210, 1110]) cube([440, 40, 30]);
}

module armcrawler_envelope() {
  color([0.16, 0.28, 0.42, 0.45])
    translate([2350, 1200, 0]) cube([700, 600, 520]);
  color([0.85, 0.32, 0.18, 0.35])
    translate([2700, 1500, 520]) sphere(r = 420, $fn = 48);
}

module sort_wall() {
  translate([4800, 280, 0]) {
    for (i = [0:6]) bin(i);
  }
}

module inbound_tote_station() {
  color([0.25, 0.25, 0.25])
    translate([280, 1180, 420]) cube([650, 460, 80]);
  color([0.4, 0.4, 0.46, 0.5])
    translate([320, 1220, 500]) cube([560, 380, 260]);
}

module cell() {
  color([0.72, 0.72, 0.68])
    translate([0, 0, -30]) cube([cell_x, cell_y, 30]);
  guard_frame();
  inbound_tote_station();
  inspection_tunnel();
  armcrawler_envelope();
  sort_wall();
}

cell();
