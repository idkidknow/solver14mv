<script lang="ts">
  import cv from '@techstark/opencv-js';
  import * as tesseract from 'tesseract.js';
	import type { Clue } from './clue';

  let { onComplete = () => {} } = $props();

  let canvas: HTMLCanvasElement;
  let debugCanvas: HTMLCanvasElement;

  const recognize = async () => {
    const image = cv.imread(canvas);
    const image1 = new cv.Mat();
    cv.cvtColor(image, image1, cv.COLOR_BGR2GRAY);
    cv.threshold(image1, image1, 200, 255, cv.THRESH_BINARY);
    cv.copyMakeBorder(image1, image1, 50, 50, 50, 50, cv.BORDER_CONSTANT, new cv.Scalar(0));
    cv.copyMakeBorder(image, image, 50, 50, 50, 50, cv.BORDER_CONSTANT, new cv.Scalar(0, 0, 0));
    cv.Canny(image1, image1, 100, 200);
    const lines = new cv.Mat();
    cv.HoughLinesP(image1, lines, 1, Math.PI / 180, 300, 300, 20);
    const linesData = lines.data32S;
    let xs: number[] = [];
    let ys: number[] = [];
    for (let i = 0; i < linesData.length; i += 4) {
      xs.push(linesData[i], linesData[i + 2]);
      ys.push(linesData[i + 1], linesData[i + 3]);
    }
    xs.sort();
    ys.sort();
    for (let i = 0; i < xs.length; i++) {
      if (i > 0 && xs[i] - Math.abs(xs[i - 1]) < 10) {
        xs[i] = -xs[i];
      }
    }
    for (let i = 0; i < ys.length; i++) {
      if (i > 0 && ys[i] - Math.abs(ys[i - 1]) < 10) {
        ys[i] = -ys[i];
      }
    }
    xs = xs.filter((x) => x > 0);
    ys = ys.filter((y) => y > 0);

    const worker = await tesseract.createWorker("eng");
    await worker.setParameters({
      tessedit_pageseg_mode: tesseract.PSM.SINGLE_CHAR,
    });

    const imageBinary = new cv.Mat();
    cv.cvtColor(image, imageBinary, cv.COLOR_BGR2GRAY);
    cv.threshold(imageBinary, imageBinary, 100, 255, cv.THRESH_BINARY_INV);
    async function checkBlock(i: number, j: number): Promise<string> {
      const rect = new cv.Rect(xs[j] + 10, ys[i] + 10, xs[j + 1] - xs[j] - 20, ys[i + 1] - ys[i] - 20);
      const img = new cv.Mat();
      imageBinary.roi(rect).copyTo(img);
      cv.copyMakeBorder(img, img, 30, 30, 30, 30, cv.BORDER_CONSTANT, new cv.Scalar(255));
      const tempCanvas = document.createElement("canvas");
      cv.imshow(tempCanvas, img);
      const text = await worker.recognize(tempCanvas).then((ret) => ret.data.text).catch(() => "");
      tempCanvas.remove();
      return text;
    }

    const m = ys.length - 1
    const n = xs.length - 1
    const clues: Clue[] = [];
    for (let i = 0; i < m; i++) {
      for (let j = 0; j < n; j++) {
        const ret = (await checkBlock(i, j)).trim();
        // console.log(i, j, ret);
        const retNum = Number(ret);
        if (!isNaN(retNum)) {
          clues.push({ type: "number", i: i, j: j, value: retNum });
        } else if (ret === "?") {
          clues.push({ type: "questionMark", i: i, j: j });
        }
      }
    }

    lines.delete();
    image1.delete();
    image.delete();

    return clues;
  };
</script>

<button onclick={async () => {
  const clues = await recognize();
  onComplete(clues);
}}>Recognize</button>
<input type="file" accept="image/*" onchange={ async (e) => {
  const file = e.currentTarget?.files?.item(0);
  if (file == null) return;
  const reader = new FileReader();
  const ret = await new Promise<string>((resolve, reject) => {
    reader.onload = () => {
      const result = reader.result;
      if (typeof result === 'string') {
        resolve(result);
      } else {
        reject();
      }
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
  const image = await new Promise<HTMLImageElement>((resolve) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.src = ret;
  });
  canvas.width = image.width;
  canvas.height = image.height;
  const ctx = canvas.getContext("2d");
  ctx?.drawImage(image, 0, 0);
}} />

<canvas bind:this={canvas}></canvas>
<canvas bind:this={debugCanvas}></canvas>
