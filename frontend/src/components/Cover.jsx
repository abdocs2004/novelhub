import { useState } from 'react'

// Same title => same colour, so every novel keeps a stable "book cover" without any image upload.
function hueFromText(text) {
  let hash = 0
  for (const char of text) hash = (hash * 31 + char.charCodeAt(0)) % 360
  return hash
}

export default function Cover({ novel, className = '' }) {
  const [imageFailed, setImageFailed] = useState(false)
  const hue = hueFromText(novel.title)

  return (
    <div
      className={`relative aspect-[2/3] overflow-hidden rounded-r-md rounded-l-sm shadow-sm ${className}`}
      style={{ backgroundColor: `hsl(${hue} 32% 28%)` }}
    >
      {novel.coverImage && !imageFailed ? (
        <img
          src={novel.coverImage}
          alt={`Cover of ${novel.title}`}
          className="h-full w-full object-cover"
          onError={() => setImageFailed(true)}
        />
      ) : (
        <div className="flex h-full flex-col justify-between p-4 pl-6 text-white">
          <span className="font-serif text-lg leading-snug font-semibold">{novel.title}</span>
          <span className="text-xs text-white/75">{novel.author}</span>
        </div>
      )}
      {/* the "spine" */}
      <span className="absolute inset-y-0 left-0 w-2 bg-black/25" aria-hidden="true" />
    </div>
  )
}
