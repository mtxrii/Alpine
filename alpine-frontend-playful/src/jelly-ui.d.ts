import type { HTMLAttributes } from 'react'

declare module 'react' {
  namespace JSX {
    interface IntrinsicElements {
      'jelly-theme': HTMLAttributes<HTMLElement> & {
        mode?: 'light' | 'dark' | 'auto'
      }
    }
  }
}
