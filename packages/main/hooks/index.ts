import useVersionHooks from './version'
import useAsstHooks from './asst'
import useStorageHooks from './storage'
import useOsHooks from './os'
import useShutdownHooks from './shutdown'
import useClearHooks from './clear'
import useTaskHooks from './task'

export default function useHooks (): void {
  useVersionHooks()
  useAsstHooks()
  useStorageHooks()
  useOsHooks()
  useShutdownHooks()
  useClearHooks()
  useTaskHooks()
}