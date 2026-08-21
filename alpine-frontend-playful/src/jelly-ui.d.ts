import type { HTMLAttributes } from 'react'

declare module 'react' {
  namespace JSX {
    interface IntrinsicElements {
      'jelly-theme': HTMLAttributes<HTMLElement> & {
        mode?: 'light' | 'dark' | 'auto'
      }
      'jelly-button': HTMLAttributes<HTMLElement> & {
        block?: boolean
        disabled?: boolean
        label?: string
        key?: string
        shape?: 'pill' | 'square'
        size?: 'small' | 'medium' | 'large'
        type?: 'button' | 'submit' | 'reset'
        variant?: 'white' | 'rose' | 'amber' | 'azure' | 'mint' | 'platinum' | 'graphite'
      }
      'jelly-input': HTMLAttributes<HTMLElement> & {
        autocomplete?: string
        disabled?: boolean
        label?: string
        name?: string
        placeholder?: string
        required?: boolean
        type?: string
        value?: string
      }
      'jelly-textarea': HTMLAttributes<HTMLElement> & {
        disabled?: boolean
        label?: string
        name?: string
        placeholder?: string
        required?: boolean
        rows?: number
        value?: string
      }
    }
  }
}
