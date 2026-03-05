{ pkgs ? import <nixpkgs> {} }:
pkgs.mkShell {
  buildInputs = with pkgs; [
    nodejs
    pnpm
    jdk25_headless
    mill
    metals
  ];
}
